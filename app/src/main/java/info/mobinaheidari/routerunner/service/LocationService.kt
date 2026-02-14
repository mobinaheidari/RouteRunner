package info.mobinaheidari.routerunner.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import info.mobinaheidari.routerunner.R
import info.mobinaheidari.routerunner.data.local.AppDao
import info.mobinaheidari.routerunner.data.local.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Foreground Service responsible for tracking the user's location in the background
 * and saving the data to the local database.
 */
@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var dao: AppDao // Injected Data Access Object via Hilt

    // Coroutine scope for background database operations, confined to the service lifecycle.
    // Uses Dispatchers.IO for disk operations and SupervisorJob to prevent child failure propagation.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentUserId: Long = -1

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the location callback.
        // Defined in onCreate to ensure it's instantiated only once during the service lifecycle.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    if (currentUserId != -1L) {
                        saveLocationToDb(location)
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START" -> {
                currentUserId = intent.getLongExtra("USER_ID", -1)
                if (currentUserId != -1L) {
                    startForegroundService()
                    startLocationUpdates()
                }
            }
            "ACTION_STOP" -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        // If the system kills the service due to memory pressure, recreate it.
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "location_channel"
        val channelName = "Route Tracking"

        val notificationManager = getSystemService(NotificationManager::class.java)

        // Create the NotificationChannel, required for Android Oreo (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RouteRunner")
            .setContentText("Tracking your run...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        // Starting Android 14 (API 34), specifying the foreground service type is mandatory.
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Interval: 5 seconds
        ).apply {
            setMinUpdateDistanceMeters(10f) // Minimum distance: 10 meters
        }.build()

        // Explicit permission check to prevent runtime crashes.
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    private fun saveLocationToDb(location: android.location.Location) {
        serviceScope.launch {
            dao.insertLocation(
                LocationEntity(
                    userId = currentUserId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        // Cancel the coroutine scope to prevent memory leaks when the service is destroyed.
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}