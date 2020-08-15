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

package com.ludoscity.herdr.common.data.network

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.RawDataCloudFolderConfiguration
import com.ludoscity.herdr.common.domain.entity.UserCredentials

interface INetworkDataPipe {
    suspend fun registerAuthClient(stackBase: String): Response<AuthClientRegistration>
    suspend fun exchangeCodeForAccessAndRefreshToken(
        authCode: String,
        authRegistrationInfo: AuthClientRegistration
    ): Response<UserCredentials>

    suspend fun refreshAccessToken(
        authRegistrationInfo: AuthClientRegistration,
        expiredCred: UserCredentials
    ): Response<UserCredentials>

    suspend fun unregisterAuthClient(authRegistrationInfo: AuthClientRegistration): Response<Unit>

    suspend fun postDirectory(stackBase: String, dirName: String, tagList: List<String>):
            Response<RawDataCloudFolderConfiguration>

    suspend fun getFileMetadata(
        stackBase: String,
        fullPathWithSlashes: String
    ): Response<RawDataCloudFolderConfiguration>
}