/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ludoscity.herdr.common.data.repository

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import io.ktor.utils.io.errors.IOException
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class LoginRepository : KoinComponent {

    private val log: Kermit by inject { parametersOf("LoginRepository") }
    private val networkDataPipe: INetworkDataPipe by inject()

    private val secureDataStore: SecureDataStore by inject()

    //memory cache
    private var authClientRegistration: AuthClientRegistration? = null
    private var userCredentials: UserCredentials? = null

    private val authClientRegistrationTokenStoreKey = "token"
    private val authClientRegistrationBaseUrlStoreKey = "base_url"
    private val authClientRegistrationRedirectUriStoreKey = "redirect_uri"
    private val authClientRegistrationClientIdStoreKey = "client_id"
    private val authClientRegistrationClientSecretStoreKey = "client_secret"

    private val userCredentialAccessTokenStoreKey = "access_token"
    private val userCredentialRefreshTokenStoreKey = "refresh_token"


    suspend fun getAuthClientRegistration(baseUrl: String, localOnly: Boolean): Response<AuthClientRegistration> {
        log.d { "About to check for existing client registration..." }
        //First see if we have in memory data...
        return if (authClientRegistration != null) {
            Response.Success(authClientRegistration!!)
        } else if (secureDataStore.retrieveString(authClientRegistrationTokenStoreKey) != null) {
            //... then see if we can retrieve data from secure storage...
            secureDataStore.apply {
                authClientRegistration = AuthClientRegistration(
                    retrieveString(authClientRegistrationBaseUrlStoreKey)!!,
                    listOf(retrieveString(authClientRegistrationRedirectUriStoreKey)!!),
                    retrieveString(authClientRegistrationTokenStoreKey)!!,
                    retrieveString(authClientRegistrationClientIdStoreKey)!!,
                    retrieveString(authClientRegistrationClientSecretStoreKey)!!
                )
            }

            Response.Success(authClientRegistration!!)
        } else if (!localOnly) {
            //...then go to network pipe if that's allowed...
            val networkReply = networkDataPipe.registerAuthClient(baseUrl)

            if (networkReply is Response.Success) {
                authClientRegistration = networkReply.data
                //TODO: store a Set of Strings
                secureDataStore.apply {
                    storeString(authClientRegistrationTokenStoreKey, authClientRegistration!!.clientRegistrationToken)
                    storeString(authClientRegistrationBaseUrlStoreKey, authClientRegistration!!.stackBaseUrl)
                    storeString(
                        authClientRegistrationRedirectUriStoreKey,
                        authClientRegistration!!.redirectUriCollection[0]
                    )
                    storeString(authClientRegistrationClientIdStoreKey, authClientRegistration!!.clientId)
                    storeString(authClientRegistrationClientSecretStoreKey, authClientRegistration!!.clientSecret)
                }
            }

            networkReply
        } else {
            //...nothing worked if we get here.
            Response.Error(IOException("localOnly==true but no local cache for OAuth client registration"))
        }
    }

    suspend fun clearAuthClientRegistration(): Response<Unit> {
        authClientRegistration?.let {
            val networkReply = networkDataPipe.unregisterAuthClient(it)

            //if we have an Error response, we'll keep registration data as is
            if (networkReply is Response.Success) {
                //got network confirmation, let's update data store and remove registration data
                secureDataStore.apply {
                    deleteKey(authClientRegistrationTokenStoreKey)
                    deleteKey(authClientRegistrationBaseUrlStoreKey)
                    deleteKey(authClientRegistrationRedirectUriStoreKey)
                    deleteKey(authClientRegistrationClientIdStoreKey)
                    deleteKey(authClientRegistrationClientSecretStoreKey)

                    deleteKey(userCredentialAccessTokenStoreKey)
                    deleteKey(userCredentialRefreshTokenStoreKey)
                }

                //clear memory cache
                authClientRegistration = null
                userCredentials = null
            }

            //forward network reply
            return networkReply
        }

        //if memory cache is already cleared, simply return Success
        return Response.Success(Unit)
    }

    suspend fun getUserCredentials(authCode: String, localOnly: Boolean)
            : Response<UserCredentials> {
        //log.d { "retrieving user credentials in repo with access token: ${userCredentials?.accessToken}" }

        return if (userCredentials != null) {
            Response.Success(userCredentials!!)
        } else if (secureDataStore.retrieveString(userCredentialAccessTokenStoreKey) != null) {
            secureDataStore.apply {
                userCredentials = UserCredentials(
                    retrieveString(userCredentialAccessTokenStoreKey)!!,
                    retrieveString(userCredentialRefreshTokenStoreKey)!!
                )
            }

            Response.Success(userCredentials!!)
        } else if (!localOnly) {
            val networkReply = networkDataPipe.exchangeCodeForAccessAndRefreshToken(authCode, authClientRegistration!!)

            if (networkReply is Response.Success) {
                userCredentials = networkReply.data
                secureDataStore.apply {
                    storeString(userCredentialAccessTokenStoreKey, userCredentials!!.accessToken)
                    storeString(userCredentialRefreshTokenStoreKey, userCredentials!!.refreshToken)
                }
            }

            networkReply
        } else {
            Response.Error(IOException("localOnly==true but no local cache for OAuth access and refresh token"))
        }
    }

    suspend fun createDirectory(): Response<String> {
        return networkDataPipe.postDirectory(authClientRegistration?.stackBaseUrl ?: "", "niceDirectory")
    }

    suspend fun refreshAccessToken(): Response<UserCredentials> {
        getAuthClientRegistration("", true)
        getUserCredentials("", true)

        val result = networkDataPipe.refreshAccessToken(authClientRegistration!!, userCredentials!!)

        if (result is Response.Success) {
            //log.d { "Updating credentials in repository with access token: ${result.data.accessToken}" }
            userCredentials = result.data
            secureDataStore.apply {
                storeString(userCredentialAccessTokenStoreKey, userCredentials!!.accessToken)
                storeString(userCredentialRefreshTokenStoreKey, userCredentials!!.refreshToken)
            }
        } else {
            Response.Error(IOException("Could not refresh token"))
        }

        return result
    }
}