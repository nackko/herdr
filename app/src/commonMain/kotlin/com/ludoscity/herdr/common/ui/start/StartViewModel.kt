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

import com.ludoscity.herdr.common.data.AnalTrackingDatapoint
import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeotrackingDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeotrackingDatapointUseCaseInput
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class StartViewModel(
    override val eventsDispatcher: EventsDispatcher<StartFragmentEventListener>
) : KoinComponent, ViewModel(), EventsDispatcherOwner<StartViewModel.StartFragmentEventListener> {

    private val saveGeotrackingDatapointUseCaseAsync: SaveGeotrackingDatapointUseCaseAsync by inject()
    private val saveAnaltrackingDatapointUseCaseAsync: SaveAnalyticsDatapointUseCaseAsync by inject()

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun onSetupButtonPressed() {
        eventsDispatcher.dispatchEvent { routeToDriveSetup() }
        //testAddGeolocationRowToDb()
        //testAddAnalyticsRowToDb()
    }

    interface StartFragmentEventListener {
        fun routeToDriveSetup()
    }

    private fun testAddGeolocationRowToDb() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val useCaseInput = SaveGeotrackingDatapointUseCaseInput(
            GeoTrackingDatapoint(-1, 666, null, 66.6, null, 66.6, 66.6, 0, "666")
        )
        val response = saveGeotrackingDatapointUseCaseAsync.execute(useCaseInput)
    }

    private fun testAddAnalyticsRowToDb() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val useCaseInput = SaveAnalyticsDatapointUseCaseInput(
            AnalTrackingDatapoint(-1, 666, "666", 66, "666", "666", "666", null, "666", 0, "666")
        )
        val response = saveAnaltrackingDatapointUseCaseAsync.execute(useCaseInput)
    }
}