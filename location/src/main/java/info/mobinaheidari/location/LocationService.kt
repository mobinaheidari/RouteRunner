package info.mobinaheidari.location

import android.annotation.SuppressLint
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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * A standalone Service that tracks GPS location and broadcasts it to the main app.
 * It has NO dependency on Room Database or Hilt.
 */
class LocationService : Service() {

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 🟢 1. Create a Scope for background tasks
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 🟢 2. Cache variables
    private var boundaryPolygon: List<LatLng> = emptyList()
    private var lastBroadcastedLocation: android.location.Location? = null

    // 🟢 ADDED: Tracking ID for multiple user support
    private var currentUserId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        android.util.Log.e("SERVICE_CHECK", "Location Service has officially CREATED")

        // 🟢 3. Load the heavy file ONCE in the background
        serviceScope.launch {
            boundaryPolygon = GeoJsonHelper.getPolygonCoordinates(applicationContext)
            android.util.Log.d("GEO_FENCE", "Polygon loaded with ${boundaryPolygon.size} points")
        }

        // Initialize the callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // 🟢 4. Check if polygon is ready
                if (boundaryPolygon.isEmpty()) {
                    android.util.Log.e("GEO_TEST", "❌ Polygon NOT loaded yet. Waiting...")
                    return
                }

                result.locations.lastOrNull()?.let { currentLocation ->
                    val userPoint = LatLng(currentLocation.latitude, currentLocation.longitude)

                    val isInside = PolyUtil.containsLocation(userPoint, boundaryPolygon, true)

                    if (isInside) {
                        android.util.Log.d("GEO_TEST", "✅ INSIDE! Saving location for User: $currentUserId")

                        // Check for transition (Outside -> Inside)
                        val wasOutside = lastBroadcastedLocation == null ||
                                !PolyUtil.containsLocation(
                                    LatLng(lastBroadcastedLocation!!.latitude, lastBroadcastedLocation!!.longitude),
                                    boundaryPolygon, true
                                )

                        if (wasOutside) {
                            android.util.Log.i("GEO_SNAP", "⚡ SNAP! Calculating edge point...")

                            val edgePoint = GeoJsonHelper.findNearestPointOnPolygon(userPoint, boundaryPolygon)

                            val edgeLocation = android.location.Location("fused").apply {
                                latitude = edgePoint.latitude
                                longitude = edgePoint.longitude
                                // Sort fix: 1 second before actual location
                                time = currentLocation.time - 1000
                            }

                            broadcastLocation(edgeLocation)
                        }

                        broadcastLocation(currentLocation)
                        lastBroadcastedLocation = currentLocation

                    } else {
                        android.util.Log.w("GEO_TEST", "⛔ OUTSIDE! Ignoring location: $userPoint")
                        lastBroadcastedLocation = null
                    }
                }
            }
        }
    }

    /**
     * Sends the location data to any app component listening for "ACTION_LOCATION_UPDATE".
     * Now correctly includes the currentUserId.
     */
    private fun broadcastLocation(location: android.location.Location) {
        val intent = Intent("ACTION_LOCATION_UPDATE").apply {
            putExtra("LAT", location.latitude)
            putExtra("LNG", location.longitude)
            putExtra("TIME", location.time)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START" -> {
                startForegroundService()
                startLocationUpdates()
            }
            "ACTION_STOP" -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "location_module_channel"
        val channelName = "GPS Tracking"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RouteRunner GPS")
            .setContentText("Tracking location in background...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
            setMinUpdateDistanceMeters(10f)
        }.build()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        // 🟢 Clean up the coroutine scope
        serviceScope.launch {
            boundaryPolygon = emptyList()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}