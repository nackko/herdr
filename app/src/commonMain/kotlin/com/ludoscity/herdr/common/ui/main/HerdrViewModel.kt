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

package com.ludoscity.herdr.common.ui.main

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseInput
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class HerdrViewModel : KoinComponent, ViewModel() {

    private val log: Kermit by inject { parametersOf("HerdrViewModel") }

    private val _loggedIn =
            MutableLiveData(
                    false
            )
    val isLoggedIn: LiveData<Boolean>
        get() = _loggedIn

    private val retrieveAccessAndRefreshTokenUseCase: RetrieveAccessAndRefreshTokenUseCaseAsync
            by inject()

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    init {
        initAuthAccessAndRefreshTokenFromCache()
    }

    private fun initAuthAccessAndRefreshTokenFromCache() = launchSilent(
            coroutineContext,
            exceptionHandler, job
    ) {
        _loggedIn.postValue(false)
        val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(true)
        val response = retrieveAccessAndRefreshTokenUseCase.execute(useCaseInput)
        processRetrieveAccessAndRefreshTokenResponse(response)
    }

    private fun processRetrieveAccessAndRefreshTokenResponse(response: Response<UserCredentials>) {
        when (response) {
            is Response.Success ->
                _loggedIn.postValue(true)
            is Response.Error ->
                _loggedIn.postValue(false)
        }
    }
}