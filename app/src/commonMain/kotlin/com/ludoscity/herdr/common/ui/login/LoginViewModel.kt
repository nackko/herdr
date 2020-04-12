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

package com.ludoscity.herdr.common.ui.login

import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.di.KodeinInjector
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.usecase.login.ExchangeCodeForAccessAndRefreshTokenUseCase
import com.ludoscity.herdr.common.domain.usecase.login.ExchangeCodeForAccessAndRefreshTokenUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.login.RegisterAuthClientUseCase
import com.ludoscity.herdr.common.domain.usecase.login.RegisterAuthClientUseCaseInput
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

class LoginViewModel : ViewModel() {

    private val _authClientRegistrationResult =
        MutableLiveData<AuthClientRegistrationState>(
            InProgressAuthClientRegistration()
        )
    val authClientRegistrationResult: LiveData<AuthClientRegistrationState>
        get() = _authClientRegistrationResult

    private val registerAuthClientUseCase by KodeinInjector.instance<RegisterAuthClientUseCase>()

    private val _userCredentials =
            MutableLiveData<UserCredentialsState>(
                    InProgressUserCredentials()
            )
    val userCredentialsResult: LiveData<UserCredentialsState>
        get() = _userCredentials

    private val exchangeCodeForAccessAndRefreshTokenUseCase
            by KodeinInjector.instance<ExchangeCodeForAccessAndRefreshTokenUseCase>()

    // ASYNC - COROUTINES
    private val coroutineContext by KodeinInjector.instance<CoroutineContext>()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun registerAuthClient(stackBaseUrl: String) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())

        val useCaseInput =
            RegisterAuthClientUseCaseInput(
                stackBaseUrl
            )
        val response = registerAuthClientUseCase.execute(useCaseInput)

        processRegistrationResponse(response)
    }

    private fun processRegistrationResponse(response: Response<AuthClientRegistration>) {
        when (response) {
            is Response.Success ->
                _authClientRegistrationResult.postValue(
                    SuccessAuthClientRegistration(
                        response
                    )
                )
            is Response.Error ->
                _authClientRegistrationResult.postValue(
                    ErrorAuthClientRegistration(
                        response
                    )
                )
        }
    }

    fun exchangeCodeForAccessAndRefreshToken(authCode: String) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _userCredentials.postValue(InProgressUserCredentials())

        val useCaseInput = ExchangeCodeForAccessAndRefreshTokenUseCaseInput(authCode)
        val response = exchangeCodeForAccessAndRefreshTokenUseCase.execute(useCaseInput)

        processCodeExchangeResponse(response)
    }

    private fun processCodeExchangeResponse(response: Response<UserCredentials>) {
        when (response) {
            is Response.Success ->
                _userCredentials.postValue(SuccessUserCredentials(response))
            is Response.Error ->
                _userCredentials.postValue(ErrorUserCredentials(response))
        }
    }

    fun setErrorUserCredentials(e: Throwable) {
        _userCredentials.postValue(ErrorUserCredentials(Response.Error(e)))
    }
}