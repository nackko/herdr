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

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.RawDataCloudFolderConfiguration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.domain.usecase.login.*
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class DriveLoginViewModel(override val eventsDispatcher: EventsDispatcher<DriveLoginFragmentEventListener>) :
    KoinComponent,
    ViewModel(),
    EventsDispatcherOwner<DriveLoginViewModel.DriveLoginFragmentEventListener> {

    private val log: Kermit by inject { parametersOf("DriveLoginViewModel") }

    private val _authClientRegistrationResult =
        MutableLiveData<AuthClientRegistrationState?>(
            null
        )

    //TODO: remove this as it exposes somewhat private data
    val authClientRegistrationResult: LiveData<AuthClientRegistrationState?>
        get() = _authClientRegistrationResult

    private val registerAuthClientUseCase: RegisterAuthClientUseCaseAsync by inject()

    private val _userCredentials =
        MutableLiveData<UserCredentialsState?>(
            null
        )
    val userCredentialsResult: LiveData<UserCredentialsState?>
        get() = _userCredentials

    //Seems to be the only way it works with xml syntax
    //See: https://github.com/icerockdev/moko-mvvm/tree/release/0.6.0#viewmodel-for-login-feature
    private val _finalUrl =
        MutableLiveData("https://username.mycozy.cloud")
    val finalUrl: LiveData<String> = _finalUrl.readOnly()

    private val retrieveAccessAndRefreshTokenUseCase: RetrieveAccessAndRefreshTokenUseCaseAsync
            by inject()

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

    //@Throws(Exception::class) //helped for ios debugging
    fun registerAuthClient() {
        viewModelScope.launch {
            log.d { "About to register auth client" }
            _authClientRegistrationResult.postValue(InProgressAuthClientRegistration())
            val useCaseInput = RegisterAuthClientUseCaseInput(_finalUrl.value)
            val response = registerAuthClientUseCase.execute(useCaseInput)
            processRegistrationResponse(response, true)
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
                    eventsDispatcher.dispatchEvent { routeToAuthFlow() }
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

    fun exchangeCodeForAccessAndRefreshToken(authCode: String) {
        viewModelScope.launch {
            _userCredentials.postValue(InProgressUserCredentials())

            val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(authCode)
            val response = retrieveAccessAndRefreshTokenUseCase.execute(useCaseInput)

            processRetrieveAccessAndRefreshTokenResponse(response)
        }
    }

    private fun processRetrieveAccessAndRefreshTokenResponse(response: Response<UserCredentials>) {
        when (response) {
            is Response.Success -> {
                _userCredentials.postValue(SuccessUserCredentials(response))
                eventsDispatcher.dispatchEvent { routeToHerdr() }
            }
            is Response.Error ->
                _userCredentials.postValue(ErrorUserCredentials(response))
        }
    }

    private fun testConnection() {
        viewModelScope.launch {

            //val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(authCode)
            //createDirectoryUseCase.execute()

            //processRetrieveAccessAndRefreshTokenResponse(response)
        }
    }

    interface DriveLoginFragmentEventListener {
        fun routeToCreateAccount()
        fun routeToHerdr()
        fun routeToAuthFlow()
    }

    companion object {
        private val URL_HTTP = Regex("^https?://", RegexOption.IGNORE_CASE)
        private val URL_PERIOD = Regex("^.*[.]", RegexOption.IGNORE_CASE)
    }
}