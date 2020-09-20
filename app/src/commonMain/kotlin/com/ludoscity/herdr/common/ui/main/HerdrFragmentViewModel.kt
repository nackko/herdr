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
import com.ludoscity.herdr.common.Timer
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.repository.UserActivityTrackingRepository
import com.ludoscity.herdr.common.domain.entity.RawDataCloudFolderConfiguration
import com.ludoscity.herdr.common.domain.usecase.analytics.*
import com.ludoscity.herdr.common.domain.usecase.login.*
import com.ludoscity.herdr.common.domain.usecase.useractivity.*
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

@ExperimentalTime
class HerdrFragmentViewModel(override val eventsDispatcher: EventsDispatcher<HerdrFragmentEventListener>) :
    KoinComponent,
    ViewModel(),
    EventsDispatcherOwner<HerdrFragmentViewModel.HerdrFragmentEventListener> {

    fun addLoggedInObserver(observer: (Boolean?) -> Unit): Response<Unit> {
        return observeLoggedInUseCaseSync.execute(ObserveLoggedInUseCaseInput(observer))
    }

    fun addUserActivityObserver(observer: (UserActivityTrackingRepository.UserActivity?) -> Unit):
            Response<Unit> {
        //log.d { "adding user activity observer" }
        return observeUserActivityUseCaseSync.execute(ObserveUserActivityUseCaseInput(observer))
    }

    fun addWillGeoTrackWalkObserver(observer: (Boolean) -> Unit): Response<Unit> {
        return observeGeoTrackUserActivityUseCaseSync.execute(
            ObserveGeoTrackUserActivityUseCaseInput(
                UserActivityTrackingRepository.UserActivity.WALK,
                observer
            )
        )
    }

    fun addWillGeoTrackRunObserver(observer: (Boolean) -> Unit): Response<Unit> {
        return observeGeoTrackUserActivityUseCaseSync.execute(
            ObserveGeoTrackUserActivityUseCaseInput(
                UserActivityTrackingRepository.UserActivity.RUN,
                observer
            )
        )
    }

    fun addWillGeoTrackBikeObserver(observer: (Boolean) -> Unit): Response<Unit> {
        return observeGeoTrackUserActivityUseCaseSync.execute(
            ObserveGeoTrackUserActivityUseCaseInput(
                UserActivityTrackingRepository.UserActivity.BIKE,
                observer
            )
        )
    }

    fun addWillGeoTrackVehicleObserver(observer: (Boolean) -> Unit): Response<Unit> {
        return observeGeoTrackUserActivityUseCaseSync.execute(
            ObserveGeoTrackUserActivityUseCaseInput(
                UserActivityTrackingRepository.UserActivity.VEHICLE,
                observer
            )
        )
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
    private val observeUserActivityUseCaseSync: ObserveUserActivityUseCaseSync by inject()
    private val observeGeoTrackUserActivityUseCaseSync: ObserveGeoTrackUserActivityUseCaseSync by inject()
    private val updateWillGeoTrackUserActivityUseCaseAsync: UpdateWillGeoTrackUserActivityUseCaseAsync by inject()
    private val retrieveLastUserActivityTimestampUseCaseAsync: RetrieveLastUserActivityTimestampUseCaseAsync by inject()
    private val retrieveUserActivityUseCaseSync: RetrieveUserActivityUseCaseSync by inject()

    private lateinit var cloudFolderId: String

    private val _stackBaseUrlText =
        MutableLiveData("https://...")
    val stackBaseUrlText: LiveData<String> = _stackBaseUrlText.readOnly()

    private val _cloudDirectoryName =
        MutableLiveData("[[dirName]]")
    val cloudDirectoryName: LiveData<String> = _cloudDirectoryName.readOnly()

    private val _walkText =
        MutableLiveData("--")
    val walkText: LiveData<String> = _walkText.readOnly()
    private val _runText =
        MutableLiveData("--")
    val runText: LiveData<String> = _runText.readOnly()
    private val _bikeText =
        MutableLiveData("--")
    val bikeText: LiveData<String> = _bikeText.readOnly()
    private val _vehicleText =
        MutableLiveData("--")
    val vehicleText: LiveData<String> = _vehicleText.readOnly()

    private val retrieveAccessAndRefreshTokenUseCase: RetrieveAccessAndRefreshTokenUseCaseAsync
            by inject()

    private val displayStringBuilder = StringBuilder()

    @ExperimentalTime
    private val timer = Timer(periodMilliSeconds = 1000) {
        recomputeDisplayStringAll(Clock.System.now().toEpochMilliseconds())
        return@Timer true
    }

    init {
        setupRemoteDirectory("herdr_raw", listOf("herdr"))
        timer.start()
    }

    private fun setupRemoteDirectory(name: String, tags: List<String>) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val useCaseInput = SetupDirectoryUseCaseInput(name, tags)
        val response = setupDirectoryUseCase.execute(useCaseInput)

        processSetupDirectoryResponse(response)
    }

    @ExperimentalTime
    private fun recomputeDisplayStringAll(now: Long) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        val currentUserActivity =
            (retrieveUserActivityUseCaseSync.execute() as Response.Success).data

        val walkResponse = retrieveLastUserActivityTimestampUseCaseAsync.execute(
            RetrieveLastUserActivityTimestampUseCaseInput(UserActivityTrackingRepository.UserActivity.WALK)
        )
        val runResponse = retrieveLastUserActivityTimestampUseCaseAsync.execute(
            RetrieveLastUserActivityTimestampUseCaseInput(UserActivityTrackingRepository.UserActivity.RUN)
        )
        val bikeResponse = retrieveLastUserActivityTimestampUseCaseAsync.execute(
            RetrieveLastUserActivityTimestampUseCaseInput(UserActivityTrackingRepository.UserActivity.BIKE)
        )
        val vehicleResponse = retrieveLastUserActivityTimestampUseCaseAsync.execute(
            RetrieveLastUserActivityTimestampUseCaseInput(UserActivityTrackingRepository.UserActivity.VEHICLE)
        )

        if (walkResponse is Response.Success &&
            runResponse is Response.Success &&
            bikeResponse is Response.Success &&
            vehicleResponse is Response.Success
        ) {

            _walkText.postValue(
                computeString(
                    walkResponse.data,
                    now,
                    currentUserActivity == UserActivityTrackingRepository.UserActivity.WALK
                )
            )
            _runText.postValue(
                computeString(
                    runResponse.data,
                    now,
                    currentUserActivity == UserActivityTrackingRepository.UserActivity.RUN
                )
            )
            _bikeText.postValue(
                computeString(
                    bikeResponse.data,
                    now,
                    currentUserActivity == UserActivityTrackingRepository.UserActivity.BIKE
                )
            )
            _vehicleText.postValue(
                computeString(
                    vehicleResponse.data,
                    now,
                    currentUserActivity == UserActivityTrackingRepository.UserActivity.VEHICLE
                )
            )
        }
    }

    @ExperimentalTime
    private fun computeString(originTimestamp: Long, now: Long, ongoing: Boolean): String {

        if (originTimestamp == -1L) {
            return "--" //TODO: maybe default could be passed along as parameter and returned here
            //that would allow platform code to use localization support from native SDKs
            //OR --> find a multiplatform solution later
        }

        val pastInstant = Instant.fromEpochMilliseconds(originTimestamp)
        val durationSinceThen = Instant.fromEpochMilliseconds(now) - pastInstant

        durationSinceThen.toComponents { days, hours, minutes, seconds, _ ->
            if (days != 0) {
                displayStringBuilder.append("${days}D ")
            }

            if (hours > 0) {
                displayStringBuilder.append("$hours::")
            }

            if (minutes < 10) {
                displayStringBuilder.append("0")
            }

            displayStringBuilder.append("${minutes}:")

            if (seconds < 10) {
                displayStringBuilder.append("0")
            }

            displayStringBuilder.append("$seconds")
        }

        val toReturn = if (ongoing) {
            "for $displayStringBuilder"
        } else {
            "$displayStringBuilder ago"
        }

        displayStringBuilder.clear()

        return toReturn
    }

    private fun processSetupDirectoryResponse(response: Response<RawDataCloudFolderConfiguration>) {
        when (response) {
            is Response.Success -> {
                log.d { "Raw data cloud folder setup response in DriveLoginViewModel: ${response.data}" }
                _cloudDirectoryName.postValue(response.data.name)
                _stackBaseUrlText.postValue(response.data.rootPath)
                cloudFolderId = response.data.id
            }
            is Response.Error -> {
                log.e { "Remote directory setup FAILURE with ${response.exception}. This is bad. Consider auto logout" }
            }
        }
    }

    fun onDriveSettingsButtonPressed() {
        eventsDispatcher.dispatchEvent {
            routeToDriveSettings(cloudFolderId)
        }
    }

    fun onUserActivityGeoTrackingSwitched(
        userActivity: UserActivityTrackingRepository.UserActivity,
        newState: Boolean
    ) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        updateWillGeoTrackUserActivityUseCaseAsync.execute(
            UpdateWillGeoTrackUserActivityUseCaseInput(
                userActivity,
                newState
            )
        )
    }

    override fun onCleared() {
        timer.stop()
        super.onCleared()
    }

    interface HerdrFragmentEventListener {
        fun routeToDriveSettings(folderId: String)
    }
}