@file:Suppress("UnusedImport")

package com.ludoscity.herdr.data.transrecognition

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.data.repository.UserActivityTrackingRepository
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.ObserveGeoTrackingUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.ObserveGeoTrackingUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeoTrackingDatapointUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.geotracking.UpdateUserLocGeoTrackingDatapointUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.useractivity.ObserveUserActivityUseCaseInput
import com.ludoscity.herdr.common.domain.usecase.useractivity.ObserveUserActivityUseCaseSync
import com.ludoscity.herdr.common.utils.launchSilent
import com.ludoscity.herdr.ui.main.HerdrActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import org.jetbrains.anko.intentFor
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by F8Full on 2019-07-02. This file is part of #findmybikes
 * Modified by F8Full on 2020-08-02. This file is part of herdr
 * A service class to monitor user activities transitions via activity transition recognition API
 * https://developer.android.com/guide/topics/location/transitions
 * As soon as it is started, it promotes itself to foreground. If repo indicates geotracking should happen
 * it subrscribes to the fusedlocaitonclient for updates every second. Being a foreground service it can do that
 */
class TransitionRecognitionService : Service(), KoinComponent {

    companion object {
        private val TAG = TransitionRecognitionService::class.java.simpleName
        var isTrackingActivityTransition = false
        private const val CHANNEL_ID = "hdr_channel_00"
        private const val PACKAGE_NAME =
            "com.ludoscity.herdr.data.transrecognition"
        private const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"
        const val FOREGROUND_SERVICE_NOTIFICATION_ID = 696

        private var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
        private var SHORTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

    }

    // ASYNC - COROUTINES
    private val coroutineContext: CoroutineContext by inject()
    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private val saveAnaltrackingDatapointUseCaseAsync:
            SaveAnalyticsDatapointUseCaseAsync by inject()
    private val updateUserLocGeoTrackingDatapointUseCaseSync:
            UpdateUserLocGeoTrackingDatapointUseCaseSync by inject()
    private val observeGeoTrackingUseCaseSync:
            ObserveGeoTrackingUseCaseSync by inject()

    private val observeUserActivityUseCaseSync:
            ObserveUserActivityUseCaseSync by inject()


    //private lateinit var repo: FindMyBikesRepository
    private lateinit var transitionRecognitionReq: ActivityTransitionRequest
    private lateinit var pendingIntent: PendingIntent
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var serviceHandler: Handler

    private lateinit var notifBuilder: NotificationCompat.Builder
    private lateinit var notifManager: NotificationManager

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private// Extra to help us figure out if we arrived in onStartCommand via the notification or not.
    // The PendingIntent that leads to a call to onStartCommand() in this service.
    // The PendingIntent to launch activity.
    // Set the Channel ID for Android O.
    // Channel ID
    val notification: Notification
        get() {
            return notifBuilder.build()
        }

    private fun initNotificationBuilder() {
        //val intent = Intent(this, LocationTrackingService::class.java)

        //TODO: user location retrieval in repo
        //val text = repo.userLocation.value?.asLatLng()?.asString()
        val text = "TODO: user loc in repo"
        //intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
        //val servicePendingIntent = PendingIntent.getService(this, 0, intent,
        //        PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, intentFor<HerdrActivity>(), 0
        )

        notifBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(
                R.drawable.ic_launch_black_24dp, getString(R.string.open_app),
                activityPendingIntent
            )
            //.addAction(R.drawable.ic_cancel_black_24dp, getString(R.string.cancel_tracing),
            //        servicePendingIntent)
            .setContentText(text)
            .setContentTitle("Content Title")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            //TODO: launcher icon
            //.setSmallIcon(R.mipmap.ic_launcher)
            .setSmallIcon(R.drawable.ic_cancel_black_24dp)
            .setTicker(text)
            .setOnlyAlertOnce(true)
            .setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifBuilder.setChannelId(CHANNEL_ID)
        }
    }

    private fun initNotificationManager() {
        notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            // Set the Notification Channel for the Notification Manager.
            notifManager.createNotificationChannel(mChannel)
        }
    }

    override fun onCreate() {
        //repo = InjectorUtils.provideRepository(application)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {

                locationResult?.let {
                    // repo should update it's userloc livedata (filtering should be aplied by observers?)
                    // it shall also insert the geopoint if tracking is in progress (which it also maintains)
                    it.locations.getOrNull(0)?.let { userLocAsLocation ->
                        updateUserLocGeoTrackingDatapointUseCaseSync.execute(
                            SaveGeoTrackingDatapointUseCaseInput(
                                GeoTrackingDatapoint(
                                    -1,
                                    -1,
                                    userLocAsLocation.altitude,
                                    userLocAsLocation.accuracy.toDouble(),
                                    //userLocAsLocation.verticalAccuracyMeters, //TODO: requires API LEVEL 26
                                    0.0f.toDouble(),
                                    userLocAsLocation.latitude,
                                    userLocAsLocation.longitude,
                                    0L,
                                    ""
                                )
                            )
                        )
                    }
                }
            }
        }

        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)

        initNotificationBuilder()
        initNotificationManager()

        observeUserActivityUseCaseSync.execute(ObserveUserActivityUseCaseInput {
            when (it) {
                UserActivityTrackingRepository.UserActivity.STILL -> notifBuilder.setContentTitle("Activity: STILL")
                UserActivityTrackingRepository.UserActivity.WALK -> notifBuilder.setContentTitle("Activity: WALK")
                UserActivityTrackingRepository.UserActivity.RUN -> notifBuilder.setContentTitle("Activity: RUN")
                UserActivityTrackingRepository.UserActivity.BIKE -> notifBuilder.setContentTitle("Activity: BIKE")
                UserActivityTrackingRepository.UserActivity.VEHICLE -> notifBuilder.setContentTitle(
                    "Activity: IN_VEHICLE"
                )
                null -> notifBuilder.setContentTitle("Activity: UNKNOWN")
            }

            notifManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notifBuilder.build())
        })

        observeGeoTrackingUseCaseSync.execute(ObserveGeoTrackingUseCaseInput {
            if (it) {
                Log.d(TAG, "Requesting location updates for geolocation trac(k)ing")
                //Utils.setRequestingLocationUpdates(this, true)
                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback, Looper.myLooper()
                    )
                    notifBuilder.setContentText("Trac(k)ing: ON")
                    notifManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notifBuilder.build())

                    saveAnalytics(
                        null,
                        "TransitionRecognitionService--requestLocationUpdates-success"
                    )
                } catch (unlikely: SecurityException) {
                    Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
                }
            } else {
                Log.d(TAG, "Removing location updates for geolocation trac(k)ing")
                try {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    //This is saved in repo (sharedPref) in the sample
                    //TODO: in our case : it meas the user wants to stop the tracing mode ??
                    //Utils.setRequestingLocationUpdates(this, false)
                    //stopSelf()
                    notifBuilder.setContentText("Trac(k)ing: OFF")
                    notifManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notifBuilder.build())

                    saveAnalytics(
                        null,
                        "TransitionRecognitionService--removeLocationUpdates-success"
                    )
                } catch (unlikely: SecurityException) {
                    Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
                }
            }
        })

        //TODO: use case to get from repo? Yes, it's like getting user credentials?
        //repo.isTrackingGeolocation.observeForever

        val transitions = mutableListOf<ActivityTransition>()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                //.setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()

        transitions +=
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()

        transitionRecognitionReq = ActivityTransitionRequest(transitions)

        val intent = applicationContext.intentFor<TransitionRecognitionIntentService>()
        pendingIntent = PendingIntent.getService(
            applicationContext,
            999, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        super.onCreate()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = SHORTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        task.result?.let {
                            updateUserLocGeoTrackingDatapointUseCaseSync.execute(
                                SaveGeoTrackingDatapointUseCaseInput(
                                    GeoTrackingDatapoint(
                                        -1,
                                        -1,
                                        it.altitude,
                                        it.accuracy.toDouble(),
                                        //userLocAsLocation.verticalAccuracyMeters, //TODO: requires API LEVEL 26
                                        0.0f.toDouble(),
                                        it.latitude,
                                        it.longitude,
                                        0L,
                                        ""
                                    )
                                )
                            )
                        }
                    } else {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        goForeground()

        startTransitionUpdate()

        return START_STICKY
    }

    override fun onDestroy() {

        stopTransitionUpdates()

        super.onDestroy()
    }

    private fun goForeground() {
        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
    }

    private fun startTransitionUpdate() {
        // Start updates
        val task = ActivityRecognition
            .getClient(this)
            .requestActivityTransitionUpdates(transitionRecognitionReq, pendingIntent)

        task.addOnSuccessListener {
            isTrackingActivityTransition = true //TODO: whys insn't that in repo?

            saveAnalytics(
                null,
                "TransitionRecognitionService--startTransitionUpdate-success"
            )
        }

        task.addOnFailureListener { e ->
            saveAnalytics(
                null,
                "TransitionRecognitionService--startTransitionUpdate-failure"
            )
            //stopSelf()
        }
    }

    private fun stopTransitionUpdates() {
        Log.e("prout", "Stopping updates")
        // Stop updates
        val task = ActivityRecognition
            .getClient(this)
            .removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            pendingIntent.cancel()
            isTrackingActivityTransition = false

            saveAnalytics(
                null,
                "TransitionRecognitionService--removeActivityTransitionUpdates-success"
            )
        }

        task.addOnFailureListener { e ->
            saveAnalytics(
                null,
                "TransitionRecognitionService--removeActivityTransitionUpdates-failure"
            )
        }
    }
}