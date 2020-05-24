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

package com.ludoscity.herdr.common.ui.drivelogin

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.di.KodeinInjector
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDataStoreUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDataStoreUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.login.*
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

class DriveLoginViewModel(secureDataStore: SecureDataStore,
    override val eventsDispatcher: EventsDispatcher<DriveLoginFragmentEventListener>)
    : ViewModel(), EventsDispatcherOwner<DriveLoginViewModel.DriveLoginFragmentEventListener> {

    private val _authClientRegistrationResult =
        MutableLiveData<AuthClientRegistrationState>(
            InProgressAuthClientRegistration()
        )
    val authClientRegistrationResult: LiveData<AuthClientRegistrationState>
        get() = _authClientRegistrationResult

    private val registerAuthClientUseCase by KodeinInjector.instance<RegisterAuthClientUseCaseAsync>()
    private val unregisterAuthClientUseCase by KodeinInjector.instance<UnregisterAuthClientUseCaseAsync>()

    private val _userCredentials =
        MutableLiveData<UserCredentialsState>(
            InProgressUserCredentials()
        )
    val userCredentialsResult: LiveData<UserCredentialsState>
        get() = _userCredentials

    private val _requestAuthFlow =
        MutableLiveData(false)
    val requestAuthFlowEvent: LiveData<Boolean>
        get() = _requestAuthFlow

    //Seems to be the only way it works with xml syntax
    //See: https://github.com/icerockdev/moko-mvvm/tree/release/0.6.0#viewmodel-for-login-feature
    private val _finalUrl =
        MutableLiveData("https://username.mycozy.cloud")
    val finalUrl: LiveData<String> = _finalUrl.readOnly()

    fun authFlowRequestProcessed() {
        _requestAuthFlow.value = false
    }

    private val retrieveAccessAndRefreshTokenUseCase
            by KodeinInjector.instance<RetrieveAccessAndRefreshTokenUseCaseAsync>()

    private val injectDataStoreUseCase by KodeinInjector.instance<InjectDataStoreUseCaseSync>()

    // ASYNC - COROUTINES
    private val coroutineContext by KodeinInjector.instance<CoroutineContext>()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }


    init {
        val input = InjectDataStoreUseCaseInput(
            secureDataStore
        )
        injectDataStoreUseCase.execute(input)
        //TODO: shall we check for injection error before proceeding. Would be hard to recover from
        initAuthClientFromCache()
        initAuthAccessAndRefreshTokenFromCache()
    }

    private fun getCozyUrl(userInput: String): String {
        return when {
            (!userInput.contains(URL_PERIOD)) && (!userInput.contains(URL_HTTP)) ->
                "https://$userInput.mycozy.cloud"
            (userInput.contains(URL_PERIOD)) && (!userInput.contains(URL_HTTP)) ->
                "https://$userInput"
            (!userInput.contains(URL_PERIOD)) && (userInput.contains(URL_HTTP)) ->
                "$userInput.mycozy.cloud"
            else -> userInput
        }

    }
        fun urlChanged(newInput: String) {
        _finalUrl.value = getCozyUrl(newInput)
    }

    private fun initAuthClientFromCache() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())
        val useCaseInput = RegisterAuthClientUseCaseInput(true)
        val response = registerAuthClientUseCase.execute(useCaseInput)
        processRegistrationResponse(response, false)
    }

    private fun initAuthAccessAndRefreshTokenFromCache() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _userCredentials.postValue(InProgressUserCredentials())
        val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(true)
        val response = retrieveAccessAndRefreshTokenUseCase.execute(useCaseInput)
        processRetrieveAccessAndRefreshTokenResponse(response)
    }

    fun registerAuthClient() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())
        val useCaseInput = RegisterAuthClientUseCaseInput(_finalUrl.value)
        val response = registerAuthClientUseCase.execute(useCaseInput)
        processRegistrationResponse(response, true)
    }

    fun unregisterAuthClient() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())
        val response = unregisterAuthClientUseCase.execute()
        processUnregisterResponse(response)
    }

    private fun processUnregisterResponse(response: Response<Unit>) {
        when (response) {
            is Response.Success -> {
                _authClientRegistrationResult.postValue(
                    ErrorAuthClientRegistration(
                        Response.Error(
                            IOException("Auth client registration cleared")
                        )
                    )
                )
                _userCredentials.postValue(
                    ErrorUserCredentials(
                        Response.Error(
                            IOException("User credentials cleared")
                        )
                    )
                )
            }
            else -> {
                //Something happened down there. Here at model level, simply repost
                //it does eat the original probably network Error in response
                _authClientRegistrationResult.postValue(authClientRegistrationResult.value)
            }
        }
    }

    private fun processRegistrationResponse(
        response: Response<AuthClientRegistration>,
        requestAuthorizationFlow: Boolean
    ) {
        when (response) {
            is Response.Success -> {
                _authClientRegistrationResult.postValue(
                    SuccessAuthClientRegistration(
                        response
                    )
                )

                if (requestAuthorizationFlow) {
                    _userCredentials.postValue(InProgressUserCredentials())
                    _requestAuthFlow.postValue(true)
                }
            }
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

        val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(authCode)
        val response = retrieveAccessAndRefreshTokenUseCase.execute(useCaseInput)

        processRetrieveAccessAndRefreshTokenResponse(response)
    }

    private fun processRetrieveAccessAndRefreshTokenResponse(response: Response<UserCredentials>) {
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

    fun onCreateAccountButtonPressed() {
        eventsDispatcher.dispatchEvent { routeToCreateAccount() }
    }

    interface DriveLoginFragmentEventListener {
        fun routeToCreateAccount()
    }

    companion object {
        private val URL_HTTP = Regex("^https?://", RegexOption.IGNORE_CASE)
        private val URL_PERIOD = Regex("^.*[.]", RegexOption.IGNORE_CASE)
    }
}