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
import com.ludoscity.herdr.common.domain.usecase.analytics.*
import com.ludoscity.herdr.common.domain.usecase.login.ObserveLoggedInUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.login.ObserveLoggedInUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseInput
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class HerdrViewModel : KoinComponent, ViewModel() {

    fun addLoggedInObserver(observer: (Boolean?) -> Unit): Response<Unit> {
        return observeLoggedInUseCaseSync.execute(ObserveLoggedInUseCaseInput(observer))
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        updatePermissionGrantedUseCaseSync.execute(UpdateLocationPermissionGrantedUseCaseInput(granted))

        if (granted) {
            log.d { "Location permission granted" }
            //saveAnalytics(null, "Location permission granted")
        } else {
            log.d { "Location permission denied" }
            //saveAnalytics(null, "Location permission denied")
        }
    }

    fun hasLocationPermission(): LiveData<Boolean> {
        return (getPermissionGrantedUseCaseSync.execute() as Response.Success).data

    }

    private val log: Kermit by inject { parametersOf("HerdrViewModel") }

    private val retrieveAccessAndRefreshTokenUseCase: RetrieveAccessAndRefreshTokenUseCaseAsync
            by inject()

    private val saveAnaltrackingDatapointUseCaseAsync:
            SaveAnalyticsDatapointUseCaseAsync by inject()

    private val observeLoggedInUseCaseSync: ObserveLoggedInUseCaseSync by inject()

    private val updatePermissionGrantedUseCaseSync: UpdatePermissionGrantedUseCaseSync by inject()
    private val getPermissionGrantedUseCaseSync: GetPermissionGrantedUseCaseSync by inject()

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    init {
        initAuthAccessAndRefreshTokenFromCache()
    }

    // So that initial logged in status happen -- shouldn't that be in init { } of repo?
    // TODO: clarify if/how *async* local storage could happen in LoginRepository init block
    private fun initAuthAccessAndRefreshTokenFromCache() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val useCaseInput = RetrieveAccessAndRefreshTokenUseCaseInput(true)
        retrieveAccessAndRefreshTokenUseCase.execute(useCaseInput)
    }
}