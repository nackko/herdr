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
data class RegistrationRequestBody(
    @SerialName("redirect_uris")
    val redirectUris: List<String> = listOf("com.ludoscity.herdr:/oauth2redirect"),
    @SerialName("client_name")
    val clientName: String = "herdr",
    @SerialName("software_id")
    val softwareId: String = "https://github.com/f8full/herdr",
    @SerialName("software_version") //see https://github.com/yshrsmz/BuildKonfig for better way?
    val softwareVersion: String,
    @SerialName("client_kind")
    val clientKind: String,
    @SerialName("client_uri")
    val clientUri: String,
    @SerialName("logo_uri")
    val logoUri: String = "https://drive.google.com/uc?export=download&id=1lhBnz4Fq_uNldcMd_1NwGC1I4RC0obgh",
    @SerialName("policy_uri")
    val policyUri: String = "TODO"
)