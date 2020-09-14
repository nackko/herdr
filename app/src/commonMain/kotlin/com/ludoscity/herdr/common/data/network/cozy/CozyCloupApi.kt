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

package com.ludoscity.herdr.common.data.network.cozy

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.Platform
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.domain.entity.*
import com.ludoscity.herdr.common.domain.usecase.login.RefreshAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseInput
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.readBytes
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.utils.io.core.toByteArray
import org.koin.core.KoinComponent
import org.koin.core.inject

class CozyCloupApi(private val log: Kermit) : KoinComponent, INetworkDataPipe {

    private val retrieveAccessAndRefreshTokenUseCaseAsync:
            RetrieveAccessAndRefreshTokenUseCaseAsync by inject()
    private val refreshAccessAndRefreshTokenUseCaseAsync:
            RefreshAccessAndRefreshTokenUseCaseAsync by inject()

    private val expiredTokenResponseContentBytes =
        "{\"error\":\"Expired token\"}\n".toByteArray()

    //TODO: should that be injected?
    private val httpClient = HttpClient {
        install(JsonFeature) {

            val truc = kotlinx.serialization.json.Json {
                this.ignoreUnknownKeys = true
            }

            serializer = KotlinxSerializer(truc)
            // Cozy replies with this somewhat strange vendor api json content type
            //solution found via https://github.com/ktorio/ktor/issues/812
            accept(ContentType.Application.Json, ContentType("application", "vnd.api+json"))
        }
        install("intercept") {

            //Add Authorization header to outgoing requests
            requestPipeline.intercept(HttpRequestPipeline.Before) {
                if (context.headers["Authorization"] == null) {
                    // unregister requests also use Authorization Bearer header
                    // but with *registration* token instead of *access* token.
                    // Such requests will have a header already set
                    // For request that were intercepted because token was expired, we nix the old
                    // access token header before retrying.
                    val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(true)
                    val userCred = retrieveAccessAndRefreshTokenUseCaseAsync.execute(useCaseInput)
                    if (userCred is Response.Success) {
                        //log.v("Network") { "About to set Auth header with token: ${userCred.data.accessToken}" }
                        context.headers["Authorization"] =
                            "Bearer ${userCred.data.accessToken}"
                    }
                }
                proceed()
            }

            //Check for expired token
            receivePipeline.intercept(HttpReceivePipeline.Phases.After) {
                if (subject.status == HttpStatusCode.BadRequest &&
                    context.response.readBytes().contentEquals(expiredTokenResponseContentBytes)
                ) {
                    //refresh access token
                    refreshAccessAndRefreshTokenUseCaseAsync.execute()

                    // nix old token from original request
                    val oldResquestBuilder = HttpRequestBuilder().takeFrom(context.request)
                    oldResquestBuilder.headers.remove("Authorization")

                    //retry
                    val retryCall = requestPipeline.execute(oldResquestBuilder, EmptyContent)
                            as HttpClientCall

                    proceedWith(retryCall.response)

                    return@intercept
                }

                proceedWith(subject)
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    log.v("Network") { message }
                }
            }

            level = LogLevel.INFO
        }
    }

    // could be split in a common login part
    override suspend fun registerAuthClient(stackBase: String): Response<AuthClientRegistration> {
        log.d { "About to register auth client" }
        try {
            //https://docs.cozy.io/en/cozy-stack/auth/#post-authregister
            val jsonServerReply = httpClient.post<RegistrationRequestSuccessReply>("$stackBase/auth/register") {
                header("Accept", "application/json")
                header("Content-Type", "application/json")

                body = RegistrationRequestBody(
                    //TODO: retrieve platform specific data
                    clientKind = "mobile-android",
                    clientUri = "https://play.google.com/store/apps/details?id=com.ludoscity.herdr",
                    softwareVersion = "[DOLLARSIGN]{BuildConfig.VERSION_NAME}"
                )
            }

            return Response.Success(
                AuthClientRegistration(
                    stackBase,
                    jsonServerReply.redirectUris,
                    jsonServerReply.registrationAccessToken,
                    jsonServerReply.clientId,
                    jsonServerReply.clientSecret
                )
            )
        } catch (e: Exception) {
            return Response.Error(exception = e, message = e.message)
        }
    }

    // could be split in a common login part
    override suspend fun unregisterAuthClient(authRegistrationInfo: AuthClientRegistration): Response<Unit> {
        return try {
            //https://docs.cozy.io/en/cozy-stack/auth/#delete-authregisterclient-id
            httpClient.delete<String>(
                "${authRegistrationInfo.stackBaseUrl}/auth/register/${authRegistrationInfo.clientId}"
            ) {
                header("Authorization", "Bearer ${authRegistrationInfo.clientRegistrationToken}")
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            log.d { "Caught exception: ${e.message}" }
            Response.Error(exception = e, message = e.message)
        }
    }

    // Cozy specific
    override suspend fun postDirectory(stackBase: String, dirName: String, tagList: List<String>):
            Response<RawDataCloudFolderConfiguration> {
        return try {
            //https://github.com/cozy/cozy-stack/blob/master/docs/files.md#post-filesdir-id
            val jsonServerReply =
                httpClient.post<CozyFileDescAnswerRoot>("$stackBase/files/") {
                    parameter("Type", "directory")
                    parameter("Name", dirName)
                    parameter("Tags", tagList.toString())
                }

            Response.Success(
                RawDataCloudFolderConfiguration(
                    jsonServerReply.data.id,
                    jsonServerReply.data.attributes.name,
                    jsonServerReply.data.attributes.path,
                    jsonServerReply.data.attributes.cozyMetadata.createdOn
                )
            )
        } catch (e: ClientRequestException) {
            //log.d(e) { "caught exception" }
            if (e.response?.status == HttpStatusCode.Conflict) {
                log.w { "caught ClientRequestException for HttpStatusCode.Conflict(409). This is recoverable" }
                Response.Error(e, HttpStatusCode.Conflict.value)
            } else {
                Response.Error(e)
            }
        } catch (e: Exception) {
            Response.Error(e)
        }
    }

    // Cozy specific
    override suspend fun postFile(
        stackBase: String,
        directoryId: String,
        filename: String,
        tagList: List<String>,
        contentJsonString: String,
        createdAt: String
    ): Response<Unit> {
        return try {
            val jsonServerReply =
                httpClient.post<String>("$stackBase/files/$directoryId") {
                    // Following gives ktor error `Header Content-Type is controlled by the engine and cannot be set explicitly`
                    //workaround (using body) was found there: https://github.com/ktorio/ktor/issues/1127
                    //header("Content-Type", "text/plain")
                    header("Content-MD5", Platform.hashBase64MD5(contentJsonString.toByteArray()).replace("\n", ""))

                    parameter("Type", "file")
                    parameter("Name", filename)
                    parameter("Tags", tagList.toString())
                    parameter("CreatedAt", Platform.toISO8601UTC(createdAt))

                    body = TextContent(contentJsonString, ContentType("text", "plain"))
                }

            log.i { "1 file uploaded of name: $filename" }
            Response.Success(Unit)
        } catch (e: ClientRequestException) {
            //log.d(e) { "caught exception" }
            if (e.response?.status == HttpStatusCode.Conflict) {
                log.w { "caught ClientRequestException for HttpStatusCode.Conflict(409). This is recoverable" }
                Response.Error(e, HttpStatusCode.Conflict.value)
            } else {
                Response.Error(e)
            }
        } catch (e: Exception) {
            log.d { "Caught exception: ${e.message}" }
            Response.Error(e)
        }
    }

    // Cozy specific
    override suspend fun getFileMetadata(stackBase: String, fullPathWithSlashes: String):
            Response<RawDataCloudFolderConfiguration> {
        return try {
            //https://github.com/cozy/cozy-stack/blob/master/docs/files.md#get-filesmetadata
            val jsonServerReply =
                httpClient.get<CozyFileDescAnswerRoot>("$stackBase/files/metadata") {
                    parameter("Path", fullPathWithSlashes)
                }

            Response.Success(
                RawDataCloudFolderConfiguration(
                    jsonServerReply.data.id,
                    jsonServerReply.data.attributes.name,
                    jsonServerReply.data.attributes.path,
                    jsonServerReply.data.attributes.cozyMetadata.createdOn
                )
            )
        } catch (e: Exception) {
            log.d { "caught exception-message: ${e.message}, e: $e" }
            Response.Error(e)
        }
    }

    // could be split in a common login part
    override suspend fun exchangeCodeForAccessAndRefreshToken(
        authCode: String,
        authRegistrationInfo: AuthClientRegistration
    ): Response<UserCredentials> {

        try {
            //https://docs.cozy.io/en/cozy-stack/auth/#post-authaccess_token
            val jsonServerReply =
                httpClient.post<TokenSuccessReply>("${authRegistrationInfo.stackBaseUrl}/auth/access_token") {

                    header("Accept", "application/json")

                    parameter("grant_type", "authorization_code")
                    parameter("code", authCode)
                    parameter("client_id", authRegistrationInfo.clientId)
                    parameter("client_secret", authRegistrationInfo.clientSecret)
                }

            return Response.Success(
                UserCredentials(
                    jsonServerReply.accessToken,
                    jsonServerReply.refreshToken!!
                )
            )

        } catch (e: Exception) {
            return Response.Error(exception = e, message = e.message)
        }
    }

    // could be split in a common login part
    override suspend fun refreshAccessToken(
        authRegistrationInfo: AuthClientRegistration,
        expiredCred: UserCredentials
    ): Response<UserCredentials> {
        try {
            log.d { "sending refresh request" }
            //https://docs.cozy.io/en/cozy-stack/auth/#post-authaccess_token
            val jsonServerReply =
                httpClient.post<TokenSuccessReply>("${authRegistrationInfo.stackBaseUrl}/auth/access_token") {

                    header("Accept", "application/json")

                    parameter("grant_type", "refresh_token")
                    parameter("refresh_token", expiredCred.refreshToken)
                    parameter("client_id", authRegistrationInfo.clientId)
                    parameter("client_secret", authRegistrationInfo.clientSecret)
                }

            return Response.Success(
                UserCredentials(
                    jsonServerReply.accessToken,
                    expiredCred.refreshToken
                )
            )
        } catch (e: Exception) {
            return Response.Error(exception = e, message = e.message)
        }
    }
}