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

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.domain.usecase.login.ExchangeCodeForAccessAndRefreshTokenUseCaseInput
import io.ktor.utils.io.errors.IOException

class LoginRepository(private val networkDataPipe: INetworkDataPipe) {

    private lateinit var secureDataStore: SecureDataStore

    fun setDataStore(storeToSet: SecureDataStore) {
        secureDataStore = storeToSet
    }

    //memory cache
    private var authClientRegistration: AuthClientRegistration? = null
    private var userCredentials: UserCredentials? = null

    private val authClientRegistrationTokenStoreKey = "token"
    private val authClientRegistrationBaseUrlStoreKey = "base_url"
    private val authClientRegistrationRedirectUriStoreKey = "redirect_uri"
    private val authClientRegistrationClientIdStoreKey = "client_id"
    private val authClientRegistrationClientSecretStoreKey = "client_secret"

    suspend fun getAuthClientRegistration(baseUrl: String, localOnly: Boolean): Response<AuthClientRegistration> {
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
                }

                //clear memory cache
                authClientRegistration = null
            }

            //forward network reply
            return networkReply
        }

        //if memory cache is already cleared, simply return Success
        return Response.Success(Unit)
    }

    suspend fun getUserCredentials(input: ExchangeCodeForAccessAndRefreshTokenUseCaseInput)
            : Response<UserCredentials> {

        return if (userCredentials == null) {
            val networkReply = networkDataPipe.exchangeCodeForAccessAndRefreshToken(input.authCode, authClientRegistration!!)

            if (networkReply is Response.Success) {
                userCredentials = networkReply.data
            }

            networkReply
        } else {
            Response.Success(userCredentials!!)
        }
    }
}