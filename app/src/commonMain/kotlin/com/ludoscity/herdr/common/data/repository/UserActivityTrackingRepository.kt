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

package com.ludoscity.herdr.common.data.repository

import co.touchlab.kermit.Kermit
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.utils.launchSilent
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext

class UserActivityTrackingRepository : KoinComponent {

    companion object {
        val willGeoTrackWalkStoreKey = "geotrackwalk"
        val willGeoTrackRunStoreKey = "geotrackrun"
        val willGeoTrackBikeStoreKey = "geotrackbike"
        val willGeoTrackVehicleStoreKey = "geotrackvehicle"

        val lastWalkChangeTimestampStoreKey = "lastwalktimestamp"
        val lastRunChangeTimestampStoreKey = "lastruntimestamp"
        val lastBikeChangeTimestampStoreKey = "lastbiketimestamp"
        val lastVehicleChangeTimestampStoreKey = "lastvehicletimestamp"
    }

    enum class UserActivity {
        STILL, WALK, RUN, BIKE, VEHICLE
    }

    private val secureDataStore: SecureDataStore by inject()


    private val log: Kermit by inject { parametersOf("ActivityTrackingRepository") }

    //private val herdrDb: HerdrDatabase by inject()

    private val _willGeoTrackWalkActivity = MutableLiveData(false)
    private val _willGeoTrackRunActivity = MutableLiveData(false)
    private val _willGeoTrackBikeActivity = MutableLiveData(false)
    private val _willGeoTrackVehicleActivity = MutableLiveData(false)

    private val _lastWalkActivityTimestamp = MutableLiveData<Long>(-1)
    private val _lastRunActivityTimestamp = MutableLiveData<Long>(-1)
    private val _lastBikeActivityTimestamp = MutableLiveData<Long>(-1)
    private val _lastVehicleActivityTimestamp = MutableLiveData<Long>(-1)

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private val _userActivity = MutableLiveData<UserActivity?>(null)
    val userActivity: LiveData<UserActivity?> = _userActivity.readOnly()

    fun addUserActivityObserver(observer: (UserActivity?) -> Unit): Response<Unit> {
        _userActivity.addObserver(observer)
        return Response.Success(Unit)
    }

    fun onNewUserActivity(userAct: UserActivity): Response<Unit> {

        when (_userActivity.value) {
            userAct -> {
                return Response.Success(Unit)
            }
            null -> {
                // update local cache
                _userActivity.postValue(userAct)

                return Response.Success(Unit)
            }
            else -> {
                _userActivity.value?.let {
                    saveUserActivityTimestamp(it)
                }

                // update local cache
                _userActivity.postValue(userAct)

                // local timestamp caches + async save to datastore
                saveUserActivityTimestamp(userAct)

                return Response.Success(Unit)
            }
        }
    }

    suspend fun onWillGeoTrackUserActivity(userActivity: UserActivity, newWillTrack: Boolean): Response<Unit>{
        //log.d { "updating geo tracking wishes" }
        when(userActivity) {
            UserActivity.STILL -> return Response.Error(IllegalArgumentException())
            UserActivity.WALK -> {
                _willGeoTrackWalkActivity.postValue(newWillTrack)
                secureDataStore.storeBoolean(willGeoTrackWalkStoreKey, newWillTrack)
            }
            UserActivity.RUN -> {
                _willGeoTrackRunActivity.postValue(newWillTrack)
                secureDataStore.storeBoolean(willGeoTrackRunStoreKey, newWillTrack)
            }
            UserActivity.BIKE -> {
                _willGeoTrackBikeActivity.postValue(newWillTrack)
                secureDataStore.storeBoolean(willGeoTrackBikeStoreKey, newWillTrack)
            }
            UserActivity.VEHICLE -> {
                _willGeoTrackVehicleActivity.postValue(newWillTrack)
                secureDataStore.storeBoolean(willGeoTrackVehicleStoreKey, newWillTrack)
            }
        }

        return Response.Success(Unit)
    }

    fun addGeoTrackActivityObserver(activity: UserActivity, observer: (Boolean) -> Unit): Response<Unit> {
        when (activity) {
            UserActivity.STILL -> return Response.Error(IllegalArgumentException())
            UserActivity.WALK -> _willGeoTrackWalkActivity.addObserver(observer)
            UserActivity.RUN -> _willGeoTrackRunActivity.addObserver(observer)
            UserActivity.BIKE -> _willGeoTrackBikeActivity.addObserver(observer)
            UserActivity.VEHICLE -> _willGeoTrackVehicleActivity.addObserver(observer)
        }

        return Response.Success(Unit)
    }

    init {
        // async load of initial status taken from keystore if available
        loadWillGeoTrackUserActivity()
        loadLastUserActivityTimestampAll()
    }

    private fun saveUserActivityTimestamp(userAct: UserActivity) = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        when (userAct) {
            UserActivity.STILL -> {
                // do nothing as we don't locally save information about STILL UserActivity
            }
            UserActivity.WALK -> {
                _lastWalkActivityTimestamp.postValue(Clock.System.now().toEpochMilliseconds())
                secureDataStore.storeLong(
                    lastWalkChangeTimestampStoreKey,
                    Clock.System.now().toEpochMilliseconds()
                )
            }
            UserActivity.RUN -> {
                _lastRunActivityTimestamp.postValue(Clock.System.now().toEpochMilliseconds())
                secureDataStore.storeLong(
                    lastRunChangeTimestampStoreKey,
                    Clock.System.now().toEpochMilliseconds()
                )
            }
            UserActivity.BIKE -> {
                _lastBikeActivityTimestamp.postValue(Clock.System.now().toEpochMilliseconds())
                secureDataStore.storeLong(
                    lastBikeChangeTimestampStoreKey,
                    Clock.System.now().toEpochMilliseconds()
                )
            }
            UserActivity.VEHICLE -> {
                _lastVehicleActivityTimestamp.postValue(Clock.System.now().toEpochMilliseconds())
                secureDataStore.storeLong(
                    lastVehicleChangeTimestampStoreKey,
                    Clock.System.now().toEpochMilliseconds()
                )
            }
        }
    }

    private fun loadLastUserActivityTimestampAll() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _lastWalkActivityTimestamp.postValue(
            secureDataStore.retrieveLong(lastWalkChangeTimestampStoreKey) ?: -1
        )
        _lastRunActivityTimestamp.postValue(
            secureDataStore.retrieveLong(lastRunChangeTimestampStoreKey) ?: -1
        )
        _lastBikeActivityTimestamp.postValue(
            secureDataStore.retrieveLong(lastBikeChangeTimestampStoreKey) ?: -1
        )
        _lastVehicleActivityTimestamp.postValue(
            secureDataStore.retrieveLong(lastVehicleChangeTimestampStoreKey) ?: -1
        )
    }

    private fun loadWillGeoTrackUserActivity() = launchSilent(
        coroutineContext,
        exceptionHandler, job
    ) {
        _willGeoTrackWalkActivity.postValue(
            secureDataStore.retrieveBoolean(willGeoTrackWalkStoreKey) ?: false
        )
        _willGeoTrackRunActivity.postValue(
            secureDataStore.retrieveBoolean(willGeoTrackRunStoreKey) ?: false
        )
        _willGeoTrackBikeActivity.postValue(
            secureDataStore.retrieveBoolean(willGeoTrackBikeStoreKey) ?: false
        )
        _willGeoTrackVehicleActivity.postValue(
            secureDataStore.retrieveBoolean(willGeoTrackVehicleStoreKey) ?: false
        )
    }
}