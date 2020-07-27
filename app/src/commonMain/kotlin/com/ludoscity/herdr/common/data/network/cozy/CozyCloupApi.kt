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
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.domain.usecase.login.RefreshAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseInput
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.readBytes
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
            serializer = KotlinxSerializer(
                Json(
                    JsonConfiguration.Stable.copy(ignoreUnknownKeys = true)
                )
            )
        }
        install("intercept") {

            //Add Authorization header to outgoing requests
            requestPipeline.intercept(HttpRequestPipeline.Before) {
                val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(true)
                val userCred = retrieveAccessAndRefreshTokenUseCaseAsync.execute(useCaseInput)
                if (userCred is Response.Success) {
                    //log.v("Network") { "About to set Auth header with token: ${userCred.data.accessToken}" }
                    context.headers["Authorization"] =
                        "Bearer ${userCred.data.accessToken}"
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

                    //retry
                    val retryCall = requestPipeline.execute(
                        HttpRequestBuilder().takeFrom(context.request),
                        EmptyContent
                    ) as HttpClientCall

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
            Response.Error(exception = e, message = e.message)
        }
    }

    override suspend fun postDirectory(stackBase: String, dirName: String): Response<String> {
        return try {
            //https://github.com/cozy/cozy-stack/blob/master/docs/files.md#post-filesdir-id
            val jsonServerReply =
                httpClient.post<String>("$stackBase/files/") {
                    parameter("Type", "directory")
                    parameter("Name", dirName)
                    parameter("Tags", "[tag0]")
                }
            Response.Success(jsonServerReply)
        } catch (e: Exception) {
            log.d(e) { "caught exception" }
            Response.Error(e)
        }
    }

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