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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequestSuccessReply(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("client_secret_expires_at")
    val clientSecretExpiresAt: Int,
    @SerialName("registration_access_token")
    val registrationAccessToken: String,
    @SerialName("grant_types")
    val grantTypes: List<String>,
    @SerialName("response_types")
    val responseTypes: List<String>,
    @SerialName("redirect_uris")
    val redirectUris: List<String>,
    @SerialName("client_name")
    val clientName: String,
    @SerialName("software_id")
    val softwareId: String,
    @SerialName("software_version")
    val softwareVersion: String,
    @SerialName("client_kind")
    val clientKind: String,
    @SerialName("client_uri")
    val clientUri: String,
    @SerialName("logo_uri")
    val logoUri: String,
    @SerialName("policy_uri")
    val policyUri: String
)