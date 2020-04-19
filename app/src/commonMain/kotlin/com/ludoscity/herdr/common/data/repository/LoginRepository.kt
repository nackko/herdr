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
import com.ludoscity.herdr.common.domain.usecase.login.RegisterAuthClientUseCaseInput

class LoginRepository(private val networkDataPipe: INetworkDataPipe) {

    private lateinit var secureDataStore: SecureDataStore

    fun setDataStore(storeToSet: SecureDataStore) {
        secureDataStore = storeToSet
    }

    //memory cache
    private var authClientRegistration: AuthClientRegistration? = null
    private var userCredentials: UserCredentials? = null

    suspend fun getAuthClientRegistration(input: RegisterAuthClientUseCaseInput): Response<AuthClientRegistration> {
        //Shall either come from memory, disk or networkDataPipe

        return if (authClientRegistration == null) {
            val networkReply = networkDataPipe.registerAuthClient(input.baseUrl)

            if (networkReply is Response.Success) {
                authClientRegistration = networkReply.data
            }

            networkReply
        } else {
            Response.Success(authClientRegistration!!)
        }

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