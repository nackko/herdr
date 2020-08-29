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

package com.ludoscity.herdr.common.ui.driveedit

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.usecase.login.UnregisterAuthClientUseCaseAsync
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

class DriveEditFragmentViewModel(override val eventsDispatcher: EventsDispatcher<DriveEditFragmentEventListener>) :
    KoinComponent,
    ViewModel(),
    EventsDispatcherOwner<DriveEditFragmentViewModel.DriveEditFragmentEventListener> {

    private val log: Kermit by inject { parametersOf("DriveEditFragmentViewModel") }

    private val unregisterAuthClientUseCase: UnregisterAuthClientUseCaseAsync by inject()

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun onLogoutButtonPressed() = launchSilent(
            coroutineContext,
            exceptionHandler, job
    ) {
        //_authClientRegistrationResult.postValue(InProgressAuthClientRegistration())
        val response = unregisterAuthClientUseCase.execute()
        processUnregisterResponse(response)
    }

    private fun processUnregisterResponse(response: Response<Unit>) {
        log.d { "About to process logout response" }
        when (response) {
            is Response.Success -> {
                log.d { "Success -- routing to startFragment" }
                eventsDispatcher.dispatchEvent { routeToStart() }
            }
            else -> {
                log.e { "Something is wrong. No changes were made (still logged in)" }
            }
        }
    }

    interface DriveEditFragmentEventListener {
        fun routeToStart()
    }
}
