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
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.dao.AnalTrackingDatapointDao
import com.ludoscity.herdr.common.data.database.dao.GeoTrackingDatapointDao
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.AnalTrackingUploadRequestBody
import com.ludoscity.herdr.common.data.network.cozy.GeoTrackingUploadRequestBody
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

// this repo can be constructed in an Android background thread context. It does not expose any LiveData or anything,
//hence the headless denomination.
// it came to be as a fix for background CoroutineWorker on the Android platform would fail because repositories
//exposing Moko mvvn (Mutable)LiveData can't be constructed on a background thread
class HeadlessRepository : KoinComponent {

    companion object {
        const val UPLOAD_GEO_PERIODIC_WORKER_UNIQUE_NAME = "herdr-upload-geo-worker"
        const val PURGE_GEO_PERIODIC_WORKER_UNIQUE_NAME = "herdr-purge-geo-worker"
        const val UPLOAD_ANAL_PERIODIC_WORKER_UNIQUE_NAME = "herdr-upload-anal-worker"
        const val PURGE_ANAL_PERIODIC_WORKER_UNIQUE_NAME = "herdr-purge-anal-worker"
    }

    private val log: Kermit by inject { parametersOf("HeadlessRepository") }

    private val herdrDb: HerdrDatabase by inject()
    private val networkDataPipe: INetworkDataPipe by inject()
    private val secureDataStore: SecureDataStore by inject()

    suspend fun retrieveWillGeoTrackUserActivity(usrActivity: UserActivityTrackingRepository.UserActivity):
            Response<Boolean> {

        when(usrActivity) {
            UserActivityTrackingRepository.UserActivity.STILL -> return Response.Error(IllegalArgumentException())
            UserActivityTrackingRepository.UserActivity.WALK -> return Response.Success(
                secureDataStore.retrieveString(
                    UserActivityTrackingRepository.willGeoTrackWalkStoreKey
                )?.toBoolean()
                    ?: false
            )
            UserActivityTrackingRepository.UserActivity.RUN -> return Response.Success(
                secureDataStore.retrieveString(
                    UserActivityTrackingRepository.willGeoTrackRunStoreKey
                )?.toBoolean()
                    ?: false
            )
            UserActivityTrackingRepository.UserActivity.BIKE -> return Response.Success(
                secureDataStore.retrieveString(
                    UserActivityTrackingRepository.willGeoTrackBikeStoreKey
                )?.toBoolean()
                    ?: false
            )
            UserActivityTrackingRepository.UserActivity.VEHICLE -> return Response.Success(
                secureDataStore.retrieveString(
                    UserActivityTrackingRepository.willGeoTrackVehicleStoreKey
                )?.toBoolean()
                    ?: false
            )
        }
    }


    suspend fun uploadAllGeoTrackingDatapointReadyForUpload(): Response<Unit> {
        val geoTrackingDao = GeoTrackingDatapointDao(herdrDb)
        val allToUpload = geoTrackingDao.selectReadyForUploadAll()

        val stackBase = secureDataStore.retrieveString(
            LoginRepository.authClientRegistrationBaseUrlStoreKey
        )

        val cloudDirectoryId = secureDataStore.retrieveString(
            LoginRepository.cloudDirectoryId
        )

        return if (allToUpload.isEmpty()) {
            log.i { "No GeoTracking records to upload, implicit success of uploading task" }
            Response.Success(Unit)
        } else if (stackBase == null) {
            log.w { "No stackbase URL, aborting upload" }
            Response.Error(IOException("No stackbase URL, aborting upload"))
        } else if (cloudDirectoryId == null) {
            log.w { "No cloudDirectoryId, aborting upload" }
            Response.Error(IOException("No cloudDirectoryId, aborting upload"))
        } else {
            log.i { "About to upload ${allToUpload.size} geolocation records" }

            var atLeastOneError = false

            allToUpload.forEach {
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
                        log.d { "Upload success, flagged geolocation record: ${it.timestamp} for deletion" }
                        geoTrackingDao.updateUploadCompleted(it.id)
                    }
                    is Response.Error -> {
                        if (networkReply.code == 409) {
                            log.i { "Recovered from 409, flagged geolocation record: ${it.timestamp} for deletion" }
                            geoTrackingDao.updateUploadCompleted(it.id)
                        } else {
                            atLeastOneError = true
                        }
                    }
                }
            }

            if (atLeastOneError) {
                Response.Error(IOException("Some error happened during the Geolocation upload process"))
            } else {
                Response.Success(Unit)
            }
        }
    }

    suspend fun uploadAllAnalTrackingDatapointReadyForUpload(): Response<Unit> {
        val analTrackingDao = AnalTrackingDatapointDao(herdrDb)
        val allToUpload = analTrackingDao.selectReadyForUploadAll()

        val stackBase = secureDataStore.retrieveString(
            LoginRepository.authClientRegistrationBaseUrlStoreKey
        )

        val cloudDirectoryId = secureDataStore.retrieveString(
            LoginRepository.cloudDirectoryId
        )

        return if (allToUpload.isEmpty()) {
            log.i { "No Analytics to upload, implicit success of uploading task" }
            Response.Success(Unit)
        } else if (stackBase == null) {
            log.w { "No stackbase URL, aborting upload" }
            Response.Error(IOException("No stackbase URL, aborting upload"))
        } else if (cloudDirectoryId == null) {
            log.w { "No cloudDirectoryId, aborting upload" }
            Response.Error(IOException("No cloudDirectoryId, aborting upload"))
        } else {
            log.i { "About to upload ${allToUpload.size} analytics records" }

            var atLeastOneError = false

            allToUpload.forEach {
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
                        log.d { "Upload success, flagged analytics record: ${it.timestamp} for deletion" }
                        analTrackingDao.updateUploadCompleted(it.id)
                    }
                    is Response.Error -> {
                        if (networkReply.code == 409) {
                            log.i { "Recovered from 409, flagged analytics record: ${it.timestamp} for deletion" }
                            analTrackingDao.updateUploadCompleted(it.id)
                        } else {
                            atLeastOneError = true
                        }
                    }
                }
            }

            if (atLeastOneError) {
                Response.Error(IOException("Some error happened during the Analytics upload process"))
            } else {
                Response.Success(Unit)
            }
        }
    }

    suspend fun purgeAllGeoTrackingDatapointAlreadyUploaded(): Response<Unit> {
        val geoTrackingDao = GeoTrackingDatapointDao(herdrDb)
        geoTrackingDao.deleteUploadedAll()
        log.i { "Geolocation table was purged with success of rows already uploaded" }
        log.i { "${geoTrackingDao.selectReadyForUploadAll().size} Geolocation records are ready for upload" }
        return Response.Success(Unit)
    }

    suspend fun purgeAllAnalTrackingDatapointAlreadyUploaded(): Response<Unit> {
        val analTrackingDao = AnalTrackingDatapointDao(herdrDb)
        analTrackingDao.deleteUploadedAll()
        log.i { "Analytics table was purged with success of rows already uploaded" }
        log.i { "${analTrackingDao.selectReadyForUploadAll().size} Analytics records are ready for upload" }
        return Response.Success(Unit)
    }
}