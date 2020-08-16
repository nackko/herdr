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
import com.ludoscity.herdr.common.domain.entity.RawDataCloudFolderConfiguration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import io.ktor.utils.io.errors.IOException
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class LoginRepository : KoinComponent {

    companion object {
        const val authClientRegistrationBaseUrlStoreKey = "base_url"
        const val cloudDirectoryId = "dir_id"
    }

    fun addLoggedInObserver(observer: (Boolean?) -> Unit): Response<Unit> {
        _loggedIn.addObserver(observer)
        return Response.Success(Unit)
    }

    private val _loggedIn =
        MutableLiveData<Boolean?>(
            null
        )

    private val _cloudDirectoryConfiguration =
        MutableLiveData<RawDataCloudFolderConfiguration?>(
            null
        )

    private val log: Kermit by inject { parametersOf("LoginRepository") }
    private val networkDataPipe: INetworkDataPipe by inject()

    private val secureDataStore: SecureDataStore by inject()

    //memory cache
    private var authClientRegistration: AuthClientRegistration? = null
    private var userCredentials: UserCredentials? = null

    private val authClientRegistrationTokenStoreKey = "token"
    private val authClientRegistrationRedirectUriStoreKey = "redirect_uri"
    private val authClientRegistrationClientIdStoreKey = "client_id"
    private val authClientRegistrationClientSecretStoreKey = "client_secret"

    private val userCredentialAccessTokenStoreKey = "access_token"
    private val userCredentialRefreshTokenStoreKey = "refresh_token"

    private val cloudDirectoryName = "dir_name"
    private val cloudDirectoryPath = "dir_path"


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
                _loggedIn.postValue(false)
            }

            //forward network reply
            return networkReply
        }

        //if memory cache is already cleared, simply return Success
        //TOTHINKABOUT: a case where authClientRegistration is null but userCredentials is NOT?
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
            _loggedIn.postValue(true)

            Response.Success(userCredentials!!)
        } else if (!localOnly) {
            val networkReply = networkDataPipe.exchangeCodeForAccessAndRefreshToken(authCode, authClientRegistration!!)

            if (networkReply is Response.Success) {
                userCredentials = networkReply.data
                _loggedIn.postValue(true)
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

    //TODO: persist configuration in secureDataStore? For now loading will happen if network connection is available
    //due to tokens being loaded from cache. See DriveLoginViewModel::processRetrieveAccessAndRefreshTokenResponse
    suspend fun setupDirectory(name: String, tags: List<String>): Response<RawDataCloudFolderConfiguration> {

        log.d { "Attempting directory setup for name: $name" }
        // check for memory cache
        _cloudDirectoryConfiguration.value?.let {
            return Response.Success(it)
        }

        // we don't check for local storage here because weird stuff could happen if folder is deleted out of the app
        // other interested parties will always go to local storage, gracefully handling having no folder
        // setup data available - certainly

        val createResult = networkDataPipe.postDirectory(authClientRegistration?.stackBaseUrl ?: "", name, tags)

        return if (createResult is Response.Success) {
            _cloudDirectoryConfiguration.postValue(createResult.data)
            log.d { "Remote directory setup success. Id saved in loginRepository and secureDataStore" }
            secureDataStore.apply {
                storeString(cloudDirectoryId, createResult.data.id)
                storeString(cloudDirectoryName, createResult.data.name)
                storeString(cloudDirectoryPath, createResult.data.path)
            }
            createResult
        } else if (createResult is Response.Error && createResult.code == 409) {

            log.d { "Trying to recover from 409-Conflict for directory with name: $name" }

            val getMetadataResult = networkDataPipe.getFileMetadata(
                authClientRegistration?.stackBaseUrl ?: "",
                "/$name"
            )

            if (getMetadataResult is Response.Success) {
                _cloudDirectoryConfiguration.postValue(getMetadataResult.data)
                secureDataStore.apply {
                    storeString(cloudDirectoryId, getMetadataResult.data.id)
                    storeString(cloudDirectoryName, getMetadataResult.data.name)
                    storeString(cloudDirectoryPath, getMetadataResult.data.path)
                }
                log.d {
                    "Raw data cloud folder setup recovery success. " +
                            "Id saved in loginRepository and secureDataStore as ${getMetadataResult.data.id}"
                }

                Response.Success(getMetadataResult.data)
            } else {
                Response.Error(IOException("Could not setup directory with name: $name"))
            }
        } else {
            Response.Error(IOException("Could not setup directory with name: $name"))
        }
    }

    suspend fun refreshAccessToken(): Response<UserCredentials> {
        getAuthClientRegistration("", true)
        getUserCredentials("", true)

        val result = networkDataPipe.refreshAccessToken(authClientRegistration!!, userCredentials!!)

        return if (result is Response.Success) {
            //log.d { "Updating credentials in repository with access token: ${result.data.accessToken}" }
            userCredentials = result.data
            secureDataStore.apply {
                storeString(userCredentialAccessTokenStoreKey, userCredentials!!.accessToken)
                storeString(userCredentialRefreshTokenStoreKey, userCredentials!!.refreshToken)
            }
            result
        } else {
            Response.Error(IOException("Could not refresh token"))
        }
    }
}