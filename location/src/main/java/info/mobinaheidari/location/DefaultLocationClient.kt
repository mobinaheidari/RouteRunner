package info.mobinaheidari.location


import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Concrete implementation of [LocationClient] that communicates with [LocationService].
 *
 * This class acts as the bridge between the UI/ViewModel and the Android Service.
 */
class DefaultLocationClient(private val context: Context) : LocationClient {

    private val TAG = "DefaultLocationClient"

    /**
     * Starts the background location tracking service.
     *
     * @param userId The unique ID of the user to ensure data is saved to the correct account.
     */
    override fun startLocationTracking() {

        val intent = Intent(context, LocationService::class.java).apply {
            action = "ACTION_START"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stops the background location tracking service.
     */
    override fun stopLocationTracking() {
        Log.d(TAG, "Stopping location tracking service.")

        val intent = Intent(context, LocationService::class.java).apply {
            action = "ACTION_STOP"
        }

        context.startService(intent)
    }
}