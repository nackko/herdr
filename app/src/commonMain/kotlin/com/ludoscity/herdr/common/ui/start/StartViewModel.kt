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

package com.ludoscity.herdr.common.ui.start

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.di.KodeinInjector
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeotrackingDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeotrackingDatapointUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDatabaseUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDatabaseUseCaseSync
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.kodein.di.erased.instance
import kotlin.coroutines.CoroutineContext

class StartViewModel(
    herdrDatabase: HerdrDatabase,
    override val eventsDispatcher: EventsDispatcher<StartFragmentEventListener>
) : ViewModel(), EventsDispatcherOwner<StartViewModel.StartFragmentEventListener> {

    private val injectDatabaseUseCase by KodeinInjector.instance<InjectDatabaseUseCaseSync>()
    private val saveGeotrackingDatapointUseCaseAsync by KodeinInjector.instance<SaveGeotrackingDatapointUseCaseAsync>()

    // ASYNC - COROUTINES
    private val coroutineContext by KodeinInjector.instance<CoroutineContext>()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    init {
        val input = InjectDatabaseUseCaseInput(herdrDatabase)
        injectDatabaseUseCase.execute(input)
    }

    fun onSetupButtonPressed() {
        eventsDispatcher.dispatchEvent { routeToDriveSetup() }
        //addRowToDb()

    }

    interface StartFragmentEventListener {
        fun routeToDriveSetup()
    }

    fun addRowToDb() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val useCaseInput = SaveGeotrackingDatapointUseCaseInput(
            GeoTrackingDatapoint.Impl(-1, 666, null, 66.6, null, 66.6, 66.6, 0, "666")
        )
        val response = saveGeotrackingDatapointUseCaseAsync.execute(useCaseInput)
        if (response is Response.Success) {
            eventsDispatcher.dispatchEvent { routeToDriveSetup() }
        } else {
            eventsDispatcher.dispatchEvent { routeToDriveSetup() }
        }
    }
}