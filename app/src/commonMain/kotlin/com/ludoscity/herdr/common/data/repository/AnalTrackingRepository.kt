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

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.AnalTrackingDatapoint
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.dao.AnalTrackingDatapointDao
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import org.koin.core.KoinComponent
import org.koin.core.inject

class AnalTrackingRepository : KoinComponent {

    private val herdrDb: HerdrDatabase by inject()

    private val _hasLocationPermission = MutableLiveData(false)
    val hasLocationPermission: LiveData<Boolean> = _hasLocationPermission

    fun onLocationPermissionAccepted(): Response<Unit> {
        _hasLocationPermission.value = true
        return Response.Success(Unit)
    }

    fun onLocationPermissionDenied(): Response<Unit> {
        _hasLocationPermission.value = false
        return Response.Success(Unit)
    }

    fun onLocationPermissionPermanentlyDenied(): Response<Unit> {
        _hasLocationPermission.value = false
        return Response.Success(Unit)
    }

    suspend fun insertAnalTrackingDatapoint(record: AnalTrackingDatapoint): Response<List<AnalTrackingDatapoint>> {
        val analTrackingDao = AnalTrackingDatapointDao(herdrDb)
        analTrackingDao.insert(record)
        return Response.Success(analTrackingDao.select())
    }

    suspend fun uploadAllAnalTrackingDatapointReadyForUpload(): Response<Unit> {

        val analTrackingDao = AnalTrackingDatapointDao(herdrDb)
        analTrackingDao.selectReadyForUploadAll().forEach {
            //networkDataPipe.uploadFile
            //if network reply is success
            analTrackingDao.updateUploadCompleted(it.id)
            //else we could maybe gather the list of everyone concerned by failure
        }

        return Response.Success(Unit)


        /*getAuthClientRegistration("", true)
        getUserCredentials("", true)

        val result = networkDataPipe.refreshAccessToken(authClientRegistration!!, userCredentials!!)

        return if (result is Response.Success) {
            //log.d { "Updating credentials in repository with access token: ${result.data.accessToken}" }
            userCredentials = result.data
            secureDataStore.apply {
                storeString(userCredentialAccessTokenStoreKey, userCredentials!!.accessToken)
                storeString(userCredentialRefreshTokenStoreKey, userCredentials!!.refreshToken)
            }
            result
        } else {
            Response.Error(IOException("Could not refresh token"))
        }*/
    }

//    @Update
//    fun update(record: AnalTrackingDatapoint)
//
//    @Query("DELETE FROM analtrackingdatapoint")
//    fun deleteAll()
//
//    @Query("DELETE FROM analtrackingdatapoint WHERE upload_completed='1'")
//    fun deleteUploadedAll()
//
//    @Query("SELECT * from analtrackingdatapoint ORDER BY id ASC")
//    fun getAllList(): LiveData<List<AnalTrackingDatapoint>>
//
//    @Query("SELECT * from analtrackingdatapoint WHERE upload_completed='0'")
//    fun getNonUploadedList(): List<AnalTrackingDatapoint>

//    private lateinit var herdrDatabase: HerdrDatabase
//
//    fun setDatabase(databaseToSet: HerdrDatabase)
}