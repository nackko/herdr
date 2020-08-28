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

package com.ludoscity.herdr.data.transrecognition

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.ludoscity.herdr.common.data.repository.UserActivityTrackingRepository

import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.UpdateGeoTrackingUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.UpdateGeoTrackingUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.useractivity.UpdateUserActivityUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.useractivity.UpdateUserActivityUseCaseSync
import com.ludoscity.herdr.common.utils.launchSilent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.jetbrains.anko.runOnUiThread
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by F8Full on 2019-07-02. This file is part of #findmybikes
 * Modified by F8Full on 2020-08-02. This file is part of herdr
 * An intent service class to monitor user activities transitions via activity transition recognition API
 * https://developer.android.com/guide/topics/location/transitions
 * The API is passed a pending intent pointing to this service. When an activity transition occurs
 * onHandleIntent is called, from which transitions can be extracted.
 */
class TransitionRecognitionIntentService :
        IntentService("TransitionRecognitionIntentService"), KoinComponent {

    companion object {
        private val TAG = TransitionRecognitionIntentService::class.java.simpleName
    }

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private val saveAnaltrackingDatapointUseCaseAsync:
            SaveAnalyticsDatapointUseCaseAsync by inject()
    private val updateGeoTrackingUseCaseSync:
            UpdateGeoTrackingUseCaseSync by inject()
    private val updateUserActivityUseCaseSync:
            UpdateUserActivityUseCaseSync by inject()


    override fun onHandleIntent(p0: Intent?) {
        p0?.let { intent ->
            //TODO: check intent.getAction
            Log.d("TransitionsIntentServic", "onHandleIntent")

            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)
                for (event in result?.transitionEvents ?: emptyList()) {
                    when (event.activityType) {
                        DetectedActivity.STILL -> {
                            Log.d("TransitionsIntentServic", "STILL")
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_ENTER")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--STILL-ACTIVITY_TRANSITION_ENTER"
                                )
                                runOnUiThread {
                                    updateGeoTrackingUseCaseSync.execute(
                                            UpdateGeoTrackingUseCaseInput(false)
                                    )
                                    updateUserActivityUseCaseSync.execute(
                                            UpdateUserActivityUseCaseInput(UserActivityTrackingRepository.UserActivity.STILL)
                                    )
                                }
                            } else {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_EXIT")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--STILL-ACTIVITY_TRANSITION_EXIT"
                                )
                            }
                        }
                        DetectedActivity.WALKING -> {
                            Log.d("TransitionsIntentServic", "WALKING")
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_ENTER")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--WALKING-ACTIVITY_TRANSITION_ENTER"
                                )
                                runOnUiThread {
                                    updateGeoTrackingUseCaseSync.execute(
                                            UpdateGeoTrackingUseCaseInput(true)
                                    )
                                    updateUserActivityUseCaseSync.execute(
                                            UpdateUserActivityUseCaseInput(UserActivityTrackingRepository.UserActivity.WALK)
                                    )
                                }
                            } else {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_EXIT")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--WALKING-ACTIVITY_TRANSITION_EXIT"
                                )
                            }
                        }
                        DetectedActivity.RUNNING -> {
                            Log.d("TransitionsIntentServic", "RUNNING")
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_ENTER")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--RUNNING-ACTIVITY_TRANSITION_ENTER"
                                )
                                runOnUiThread {
                                    updateGeoTrackingUseCaseSync.execute(
                                            UpdateGeoTrackingUseCaseInput(true)
                                    )
                                    updateUserActivityUseCaseSync.execute(
                                            UpdateUserActivityUseCaseInput(UserActivityTrackingRepository.UserActivity.RUN)
                                    )
                                }
                            } else {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_EXIT")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--WALKING-ACTIVITY_TRANSITION_EXIT"
                                )
                            }
                        }
                        DetectedActivity.ON_BICYCLE -> {
                            Log.d("TransitionsIntentServic", "ON_BICYCLE")
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_ENTER")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--ON_BICYCLE-ACTIVITY_TRANSITION_ENTER"
                                )
                                runOnUiThread {
                                    updateGeoTrackingUseCaseSync.execute(
                                            //UpdateGeoTrackingUseCaseInput(true)
                                            UpdateGeoTrackingUseCaseInput(false)
                                    )
                                    updateUserActivityUseCaseSync.execute(
                                            UpdateUserActivityUseCaseInput(UserActivityTrackingRepository.UserActivity.BIKE)
                                    )
                                }
                            } else {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_EXIT")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--ON_BICYCLE-ACTIVITY_TRANSITION_EXIT"
                                )
                            }
                        }
                        DetectedActivity.IN_VEHICLE -> {
                            Log.d("TransitionsIntentServic", "IN_VEHICLE")
                            if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_ENTER")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--IN_VEHICLE-ACTIVITY_TRANSITION_ENTER"
                                )
                                runOnUiThread {
                                    updateGeoTrackingUseCaseSync.execute(
                                            UpdateGeoTrackingUseCaseInput(true)
                                    )
                                    updateUserActivityUseCaseSync.execute(
                                            UpdateUserActivityUseCaseInput(UserActivityTrackingRepository.UserActivity.VEHICLE)
                                    )
                                }
                            } else {
                                Log.d("TransitionsIntentServic", "ACTIVITY_TRANSITION_EXIT")
                                saveAnalytics(
                                        null,
                                        "$TAG::onHandleIntent--IN_VEHICLE-ACTIVITY_TRANSITION_EXIT"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //FIXME: actual value always being null see
    // https://github.com/f8full/findmybikes/blob/be3e9504d2441e0c0a661bee88bf6ca7276d2c14/app/src/main/java/com/ludoscity/findmybikes/data/database/tracking/AnalTrackingDatapoint.kt#L47
    private fun saveAnalytics(batteryChargePercentage: Long? = null,
                              description: String) = launchSilent(
            coroutineContext,
            exceptionHandler, job
    ) {
        /*val response = */saveAnaltrackingDatapointUseCaseAsync.execute(
                SaveAnalyticsDatapointUseCaseInput(batteryChargePercentage, description)
        )
    }
}