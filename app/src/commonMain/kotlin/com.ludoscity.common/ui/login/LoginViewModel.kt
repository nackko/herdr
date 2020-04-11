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

package com.ludoscity.common.ui.login

import com.ludoscity.common.base.Response
import com.ludoscity.common.di.KodeinInjector
import com.ludoscity.common.domain.entity.AuthClientRegistration
import com.ludoscity.common.domain.usecase.login.RegisterAuthClientUseCase
import com.ludoscity.common.domain.usecase.login.RegisterAuthClientUseCaseInput
import com.ludoscity.common.utils.launchSilent
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

class LoginViewModel : ViewModel() {

    private val _authClientRegistrationResult =
        MutableLiveData<AuthClientRegistrationState>(InProgressAuthClientRegistration())
    val authClientRegistrationResult: LiveData<AuthClientRegistrationState>
        get() = _authClientRegistrationResult

    private val registerAuthClientUseCase by KodeinInjector.instance<RegisterAuthClientUseCase>()

    // ASYNC - COROUTINES
    private val coroutineContext by KodeinInjector.instance<CoroutineContext>()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun registerAuthClient(stackBaseUrl: String) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())

        val useCaseInput = RegisterAuthClientUseCaseInput(stackBaseUrl)
        val response = registerAuthClientUseCase.execute(useCaseInput)

        processRegistrationResponse(response)
    }

    private fun processRegistrationResponse(response: Response<AuthClientRegistration>) {
        when (response) {
            is Response.Success ->
                _authClientRegistrationResult.postValue(SuccessAuthClientRegistration(response))
            is Response.Error ->
                _authClientRegistrationResult.postValue(ErrorAuthClientRegistration(response))
        }
    }
}