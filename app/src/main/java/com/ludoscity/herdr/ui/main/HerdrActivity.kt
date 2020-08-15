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

package com.ludoscity.herdr.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.data.repository.AnalTrackingRepository.Companion.UPLOAD_ANAL_PERIODIC_WORKER_UNIQUE_NAME
import com.ludoscity.herdr.common.ui.main.HerdrViewModel
import com.ludoscity.herdr.data.AnalTrackingUploadWorker
import com.ludoscity.herdr.data.transrecognition.TransitionRecognitionService
import com.ludoscity.herdr.databinding.ActivityHerdrBinding
import com.ludoscity.herdr.utils.startServiceForeground
import dev.icerock.moko.mvvm.MvvmActivity
import dev.icerock.moko.mvvm.createViewModelFactory
import org.jetbrains.anko.intentFor
import java.util.concurrent.TimeUnit

class HerdrActivity : MvvmActivity<ActivityHerdrBinding, HerdrViewModel>() {

    override val layoutId: Int = R.layout.activity_herdr
    override val viewModelVariableId: Int = com.ludoscity.herdr.BR.herdrViewModel
    override val viewModelClass: Class<HerdrViewModel> = HerdrViewModel::class.java

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory { HerdrViewModel() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: loggedIn signal is only really useful for data upload
        // app already can trac(k)e user activity changes (bike <--> walk <--> ...) and trac(k)e geolocation (GPS)
        // and persist in local db.
        // For now, logging out disconnects both tracking, nothing gets persisted to db
        viewModel.addLoggedInObserver { loggedIn ->
            val workManager = WorkManager.getInstance(applicationContext)

            if (loggedIn == true) {
                startServiceForeground(intentFor<TransitionRecognitionService>())

                Log.d("TAG", "Setting up upload and purge tasks")
                //WorkManager task for periodic db upload
                //https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                //TODO: intervals should be different in debug and release builds.
                // Debug upload and purge more frequently
                val uploadAnalRequest =
                    PeriodicWorkRequestBuilder<AnalTrackingUploadWorker>(1, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build()

                workManager.enqueueUniquePeriodicWork(
                    UPLOAD_ANAL_PERIODIC_WORKER_UNIQUE_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    uploadAnalRequest
                )
            } else {
                stopService(intentFor<TransitionRecognitionService>())

                Log.d("TAG", "Cancelling analytics uploading recurring task")
                workManager.cancelUniqueWork(UPLOAD_ANAL_PERIODIC_WORKER_UNIQUE_NAME)
            }
        }

        viewModel.setLocationPermissionGranted(
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    override fun onResume() {
        if (!viewModel.hasLocationPermission().value) {
            val request = permissionsBuilder(android.Manifest.permission.ACCESS_FINE_LOCATION).build()

            //log.i(TAG, "Sending location permission request")
            request.send()

            request.listeners {
                onAccepted { viewModel.setLocationPermissionGranted(true) }
                onDenied { viewModel.setLocationPermissionGranted(false) }
                onPermanentlyDenied { viewModel.setLocationPermissionGranted(false) }
                //onShouldShowRationale { perms, nonce ->
                //}
            }
        } /*else {
            Log.i(TAG, "Activity was resumed and already have location permission, carrying on...")
        }*/

        super.onResume()
    }
}