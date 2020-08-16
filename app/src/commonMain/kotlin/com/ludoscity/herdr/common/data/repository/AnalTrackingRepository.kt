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
import com.ludoscity.herdr.common.data.AnalTrackingDatapoint
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.dao.AnalTrackingDatapointDao
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.AnalTrackingUploadRequestBody
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class AnalTrackingRepository : KoinComponent {

    companion object {
        const val UPLOAD_ANAL_PERIODIC_WORKER_UNIQUE_NAME = "herdr-upload-anal-worker"
    }

    private val log: Kermit by inject { parametersOf("AnalTrackingRepository") }

    private val herdrDb: HerdrDatabase by inject()
    private val networkDataPipe: INetworkDataPipe by inject()
    private val secureDataStore: SecureDataStore by inject()

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
        var atLeastOneError = true
        analTrackingDao.selectReadyForUploadAll().forEach {
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
                        "${it.timestamp}_ANALYTICS.json",
                        listOf("herdr", "analytics"),
                        Json(JsonConfiguration.Stable).stringify(
                            AnalTrackingUploadRequestBody.serializer(),
                            AnalTrackingUploadRequestBody(
                                it.timestamp_epoch,
                                it.app_version,
                                it.api_level,
                                it.device_model,
                                it.language,
                                it.country,
                                it.battery_charge_percentage,
                                it.description,
                                it.timestamp
                            )
                        ),
                        it.timestamp
                    )

                    when (networkReply) {
                        is Response.Success -> {
                            log.d { "Upload success, flagged record of name: ${it.timestamp}_ANALYTICS.json for deletion" }
                            analTrackingDao.updateUploadCompleted(it.id)
                            atLeastOneError = false
                        }
                        is Response.Error -> {
                            if (networkReply.code == 409) {
                                log.i { "Recovered from 409, flagged record: ${it.timestamp}_ANALYTICS.json for deletion" }
                                analTrackingDao.updateUploadCompleted(it.id)
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