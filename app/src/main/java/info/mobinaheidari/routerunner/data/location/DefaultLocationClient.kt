package info.mobinaheidari.routerunner.data.location

import android.content.Context
import android.content.Intent
import android.os.Build
import info.mobinaheidari.routerunner.domain.location.LocationClient
import info.mobinaheidari.routerunner.service.LocationService

/**
 * Concrete implementation of the [LocationClient] interface.
 *
 * This class resides in the **Data Layer** and is responsible for the actual interaction
 * with the Android System to manage the [LocationService]. It abstracts away the complexity
 * of [Intent] creation and API-level checks from the rest of the application.
 *
 * @property context The application context, used to start services safely without leaking activity contexts.
 */
class DefaultLocationClient(
    private val context: Context
) : LocationClient {

    /**
     * Starts the background location tracking service.
     *
     * This method constructs an explicit [Intent] targeting [LocationService], attaches
     * the [userId] as an extra, and sets the action to "ACTION_START".
     *
     * **API Compatibility:**
     * - For Android O (API 26) and above, it uses [startForegroundService] to comply with background execution limits.
     * - For older versions, it falls back to the standard [startService].
     *
     * @param userId The ID of the user for whom location data will be recorded.
     */
    override fun startLocationTracking(userId: Long) {
        val intent = Intent(context, LocationService::class.java).apply {
            action = "ACTION_START"
            putExtra("USER_ID", userId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Signals the location service to stop tracking.
     *
     * Sends an [Intent] with "ACTION_STOP" to the service. The service itself is responsible
     * for tearing down the notification and stopping location updates upon receiving this command.
     */
    override fun stopLocationTracking() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = "ACTION_STOP"
        }
        context.startService(intent)
    }
}