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

package com.ludoscity.herdr.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.usecase.geotracking.PurgeAllGeoTrackingDatapointUseCaseAsync
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by F8Full on 2019-06-28.
 *
 * Modified by F8Full on 2020-08-16. This file is part of herdr
 */
class GeoTrackingPurgeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val purgeAllGeoTrackingDatapointUseCaseAsync: PurgeAllGeoTrackingDatapointUseCaseAsync
            by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Do the work here--in this case, purge the data
        Log.i(GeoTrackingPurgeWorker::class.java.simpleName, "About to purge geolocation table")

        if (purgeAllGeoTrackingDatapointUseCaseAsync.execute() is Response.Success) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}