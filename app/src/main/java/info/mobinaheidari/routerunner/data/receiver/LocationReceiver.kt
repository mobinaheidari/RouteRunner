package info.mobinaheidari.routerunner.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.mobinaheidari.routerunner.data.local.AppDao
import info.mobinaheidari.routerunner.data.local.LocationEntity
import info.mobinaheidari.routerunner.data.local.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationReceiver @Inject constructor(
    private val dao: AppDao,
    private val sessionManager: SessionManager // 🟢 Inject the Session Manager
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_LOCATION_UPDATE") {
            val lat = intent.getDoubleExtra("LAT", 0.0)
            val lng = intent.getDoubleExtra("LNG", 0.0)
            val time = intent.getLongExtra("TIME", System.currentTimeMillis())

            // 🟢 Get the active user from local memory, not the broadcast!
            val activeUserId = sessionManager.getActiveUserId()

            if (activeUserId != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insertLocation(
                        LocationEntity(
                            userId = activeUserId,
                            latitude = lat,
                            longitude = lng,
                            timestamp = time
                        )
                    )
                }
            }
        }
    }
}