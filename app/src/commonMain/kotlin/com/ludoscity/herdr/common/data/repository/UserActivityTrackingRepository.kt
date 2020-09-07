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
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
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

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private val _userActivity = MutableLiveData<UserActivity?>(null)
    //val userActivity: LiveData<UserActivity?>
    //    get() = _userActivity

    fun addUserActivityObserver(observer: (UserActivity?) -> Unit): Response<Unit> {
        _userActivity.addObserver(observer)
        return Response.Success(Unit)
    }

    fun onNewUserActivity(userAct: UserActivity): Response<Unit> {

        // update local cache
        _userActivity.postValue(userAct)

        return Response.Success(Unit)
    }

    suspend fun onWillGeoTrackUserActivity(userActivity: UserActivity, newWillTrack: Boolean): Response<Unit>{
        log.d { "updating geo tracking wishes" }
        when(userActivity) {
            UserActivity.STILL -> return Response.Error(IllegalArgumentException())
            UserActivity.WALK -> {
                _willGeoTrackWalkActivity.postValue(newWillTrack)
                secureDataStore.storeString(willGeoTrackWalkStoreKey, newWillTrack.toString())
            }
            UserActivity.RUN ->  {
                _willGeoTrackRunActivity.postValue(newWillTrack)
                secureDataStore.storeString(willGeoTrackRunStoreKey, newWillTrack.toString())
            }
            UserActivity.BIKE ->  {
                _willGeoTrackBikeActivity.postValue(newWillTrack)
                secureDataStore.storeString(willGeoTrackBikeStoreKey, newWillTrack.toString())
            }
            UserActivity.VEHICLE -> {
                _willGeoTrackVehicleActivity.postValue(newWillTrack)
                secureDataStore.storeString(willGeoTrackVehicleStoreKey, newWillTrack.toString())
            }
        }

        return Response.Success(Unit)
    }

    fun addGeoTrackActivityObserver(activity: UserActivity, observer: (Boolean) -> Unit): Response<Unit> {
        when(activity) {
            UserActivity.STILL -> return Response.Error(IllegalArgumentException())
            UserActivity.WALK -> { _willGeoTrackWalkActivity.addObserver(observer)}
            UserActivity.RUN -> { _willGeoTrackRunActivity.addObserver(observer)}
            UserActivity.BIKE -> { _willGeoTrackBikeActivity.addObserver(observer)}
            UserActivity.VEHICLE -> { _willGeoTrackVehicleActivity.addObserver(observer)}
        }

        return Response.Success(Unit)
    }

    init {
        // async load of initial status taken from keystore if available
        loadWillGeoTrackUserActivity()
    }

    private fun loadWillGeoTrackUserActivity() = launchSilent(
            coroutineContext,
            exceptionHandler, job
    ) {
        _willGeoTrackWalkActivity.postValue(
                secureDataStore.retrieveString(willGeoTrackWalkStoreKey)?.toBoolean() ?: false
        )
        _willGeoTrackRunActivity.postValue(
                secureDataStore.retrieveString(willGeoTrackRunStoreKey)?.toBoolean() ?: false
        )
        _willGeoTrackBikeActivity.postValue(
            secureDataStore.retrieveString(willGeoTrackBikeStoreKey)?.toBoolean() ?: false
        )
        _willGeoTrackVehicleActivity.postValue(
            secureDataStore.retrieveString(willGeoTrackVehicleStoreKey)?.toBoolean() ?: false
        )
    }
}