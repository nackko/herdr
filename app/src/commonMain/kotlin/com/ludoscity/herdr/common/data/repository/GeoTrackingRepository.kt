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
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class GeoTrackingRepository : KoinComponent {

    companion object {
        const val UPLOAD_GEO_PERIODIC_WORKER_UNIQUE_NAME = "herdr-upload-geo-worker"
        const val PURGE_GEO_PERIODIC_WORKER_UNIQUE_NAME = "herdr-purge-geo-worker"
    }

    private val log: Kermit by inject { parametersOf("GeoTrackingRepository") }

    private val herdrDb: HerdrDatabase by inject()
    private val networkDataPipe: INetworkDataPipe by inject()
    private val secureDataStore: SecureDataStore by inject()


    private val _userLoc = MutableLiveData<GeoTrackingDatapoint?>(null)
    /*val userLocation: LiveData<GeoTrackingDatapoint?>
        get() = _userLoc*/

    private val _isTrackingGeolocation = MutableLiveData(false)
    //val isTrackingGeolocation: LiveData<Boolean> = _isTrackingGeolocation

    fun addGeoTrackingObserver(observer: (Boolean) -> Unit): Response<Unit> {
        _isTrackingGeolocation.addObserver(observer)
        return Response.Success(Unit)
    }

    fun updateGeoTracking(newState: Boolean): Response<Unit> {
        _isTrackingGeolocation.postValue(newState)
        return Response.Success(Unit)
    }

    fun onNewUserLocation(userLoc: GeoTrackingDatapoint): Response<GeoTrackingDatapoint> {

        //if geolocation tracking, also put in database
        if (_isTrackingGeolocation.value) {
            insertGeoTrackingDatapoint(userLoc)
        }

        // update local cache
        _userLoc.postValue(userLoc)

        return Response.Success(userLoc)
    }

    fun insertGeoTrackingDatapoint(record: GeoTrackingDatapoint): Response<List<GeoTrackingDatapoint>> {
        val geoTrackingDao = GeoTrackingDatapointDao(herdrDb)
        //TODO: retrieve timestamp epoch here for both platforms at once
        geoTrackingDao.insert(record)
        return Response.Success(geoTrackingDao.select())
    }

    suspend fun uploadAllGeoTrackingDatapointReadyForUpload(): Response<Unit> {
        val geoTrackingDao = GeoTrackingDatapointDao(herdrDb)
        var atLeastOneError = true
        geoTrackingDao.selectReadyForUploadAll().forEach {
            secureDataStore.retrieveString(
                LoginRepository.authClientRegistrationBaseUrlStoreKey
            )?.let { stackBase ->
                log.d { "We have a stack address" }
                secureDataStore.retrieveString(
                    LoginRepository.cloudDirectoryId
                )?.let { cloudDirectoryId ->
                    log.d { "We have a directory id" }
                    val networkReply = networkDataPipe.postFile(
                        stackBase,
                        cloudDirectoryId,
                        "${it.timestamp}_GEOLOCATION.json",
                        listOf("herdr", "geolocation"),
                        Json(JsonConfiguration.Stable).stringify(
                            GeoTrackingUploadRequestBody.serializer(),
                            GeoTrackingUploadRequestBody(
                                it.timestamp_epoch,
                                it.altitude,
                                it.accuracy_horizontal_meters,
                                it.accuracy_vertical_meters,
                                it.latitude,
                                it.longitude,
                                it.timestamp
                            )
                        ),
                        it.timestamp
                    )

                    when (networkReply) {
                        is Response.Success -> {
                            log.d { "Upload success, flagged record of name: ${it.timestamp}_GEOLOCATION.json for deletion" }
                            geoTrackingDao.updateUploadCompleted(it.id)
                            atLeastOneError = false
                        }
                        is Response.Error -> {
                            if (networkReply.code == 409) {
                                log.i { "Recovered from 409, flagged record: ${it.timestamp}_GEOLOCATION.json for deletion" }
                                geoTrackingDao.updateUploadCompleted(it.id)
                                atLeastOneError = false
                            } /*else { //default is true
                                atLeastOneError = true
                            }*/
                        }
                    }
                }
            }
        }

        return if (atLeastOneError) {
            Response.Error(IOException("Some error happened during the upload process"))
        } else {
            Response.Success(Unit)
        }
    }

    suspend fun purgeAllGeoTrackingDatapointAlreadyUploaded(): Response<Unit> {
        GeoTrackingDatapointDao(herdrDb).deleteUploadedAll()
        log.i { "Geolocation table was purged with success" }
        return Response.Success(Unit)
    }

//    @Update
//    fun update(record: GeoTrackingDatapoint)
//
//    @Query("DELETE FROM geotrackingdatapoint")
//    fun deleteAll()
//
//    @Query("DELETE FROM geotrackingdatapoint WHERE upload_completed='1'")
//    fun deleteUploadedAll()
//
//    @Query("SELECT * from geotrackingdatapoint ORDER BY id ASC")
//    fun getAllList(): LiveData<List<GeoTrackingDatapoint>>
//
//    @Query("SELECT * from geotrackingdatapoint WHERE upload_completed='0'")
//    fun getNonUploadedList(): List<GeoTrackingDatapoint>

//    private lateinit var herdrDatabase: HerdrDatabase
//
//    fun setDatabase(databaseToSet: HerdrDatabase)
}