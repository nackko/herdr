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
import com.ludoscity.herdr.common.domain.entity.RawDataCloudFolderConfiguration
import com.ludoscity.herdr.common.domain.usecase.analytics.*
import com.ludoscity.herdr.common.domain.usecase.login.*
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class HerdrFragmentViewModel(override val eventsDispatcher: EventsDispatcher<HerdrFragmentEventListener>) :
        KoinComponent,
        ViewModel(),
        EventsDispatcherOwner<HerdrFragmentViewModel.HerdrFragmentEventListener> {

    fun addLoggedInObserver(observer: (Boolean?) -> Unit): Response<Unit> {
        return observeLoggedInUseCaseSync.execute(ObserveLoggedInUseCaseInput(observer))
    }

    private val log: Kermit by inject { parametersOf("HerdrFragmentViewModel") }

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private val setupDirectoryUseCase: SetupDirectoryUseCaseAsync by inject()

    private val saveAnaltrackingDatapointUseCaseAsync:
            SaveAnalyticsDatapointUseCaseAsync by inject()

    private val observeLoggedInUseCaseSync: ObserveLoggedInUseCaseSync by inject()

    private val retrieveAccessAndRefreshTokenUseCase: RetrieveAccessAndRefreshTokenUseCaseAsync
            by inject()

    init {
        setupRemoteDirectory("herdr_raw", listOf("herdr"))
    }

    private fun setupRemoteDirectory(name: String, tags: List<String>) = launchSilent(
            coroutineContext,
            exceptionHandler, job
    ) {
        val useCaseInput = SetupDirectoryUseCaseInput(name, tags)
        val response = setupDirectoryUseCase.execute(useCaseInput)

        processSetupDirectoryResponse(response)
    }

    private fun processSetupDirectoryResponse(response: Response<RawDataCloudFolderConfiguration>) {
        when (response) {
            is Response.Success -> {
                log.d { "Raw data cloud folder setup response in DriveLoginViewModel: ${response.data}" }
            }
            is Response.Error -> {
                log.e { "Remote directory setup FAILURE with ${response.exception}. This is bad. Consider auto logout" }
            }
        }
    }

    fun onLogoutButtonPressed() {
        eventsDispatcher.dispatchEvent { routeToDriveLogin() }
    }

    interface HerdrFragmentEventListener {
        fun routeToDriveLogin()
        //fun routeToCreateAccount()
    }
}