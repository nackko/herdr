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

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class CozyCloupApi {

    //TODO: should that be injected?
    private val httpClient = HttpClient()

    //TODO: consider using Url type instead of String
    suspend fun getOauthClientRegistration(stackBase: String): Response<AuthClientRegistration> {

        try {
            //https://docs.cozy.io/en/cozy-stack/auth/#post-authregister
            val jsonServerReply = httpClient.post<String>("$stackBase/auth/register") {
                header("Accept", "application/json")
                header("Content-Type", "application/json")
                body = Json(JsonConfiguration.Stable).stringify(
                    RegistrationRequestBody.serializer(),
                    RegistrationRequestBody(
                        //TODO: retrieve platform specific data
                        clientKind = "mobile-android",
                        clientUri = "https://play.google.com/store/apps/details?id=com.ludoscity.herdr",
                        softwareVersion = "[DOLLARSIGN]{BuildConfig.VERSION_NAME}"
                    )
                )
            }

            val parsedReply = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))
                .parse(RegistrationRequestSuccessReply.serializer(), jsonServerReply)

            /////////////

            //////

            return Response.Success(
                AuthClientRegistration(
                    stackBase,
                    parsedReply.redirectUris,
                    parsedReply.registrationAccessToken,
                    parsedReply.clientId,
                    parsedReply.clientSecret
                )
            )
        } catch (e: Exception) {
            return Response.Error(exception = e, message = e.message)
        }
    }

    suspend fun unregisterOAuthClient(registrationInfo: AuthClientRegistration):
            Response<Unit> {

        return try {
            //https://docs.cozy.io/en/cozy-stack/auth/#delete-authregisterclient-id
            httpClient.delete<String>(
                "${registrationInfo.stackBaseUrl}/auth/register/${registrationInfo.clientId}"
            ) {
                header("Authorization", "Bearer ${registrationInfo.clientRegistrationToken}")
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(exception = e, message = e.message)
        }
    }

    suspend fun exchangeCodeForAccessAndRefreshToken(
        authCode: String,
        authRegistrationInfo: AuthClientRegistration
    ): Response<UserCredentials> {

        try {
            //https://docs.cozy.io/en/cozy-stack/auth/#post-authaccess_token
            val jsonServerReply =
                httpClient.post<String>("${authRegistrationInfo.stackBaseUrl}/auth/access_token") {

                    header("Accept", "application/json")

                    parameter("grant_type", "authorization_code")
                    parameter("code", authCode)
                    parameter("client_id", authRegistrationInfo.clientId)
                    parameter("client_secret", authRegistrationInfo.clientSecret)
            }

            val parsedReply = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))
                .parse(TokenExchangeSuccessReply.serializer(), jsonServerReply)

            return Response.Success(UserCredentials(
                parsedReply.accessToken,
                parsedReply.refreshToken
            ))

        } catch (e: Exception) {
            return Response.Error(exception = e, message = e.message)
        }
    }
}