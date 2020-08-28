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
import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.dao.GeoTrackingDatapointDao
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.GeoTrackingUploadRequestBody
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class UserActivityTrackingRepository : KoinComponent {

    enum class UserActivity {
        STILL, WALK, RUN, BIKE, VEHICLE
    }

    private val log: Kermit by inject { parametersOf("ActivityTrackingRepository") }

    //private val herdrDb: HerdrDatabase by inject()


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
}