package info.mobinaheidari.routerunner.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun saveActiveUserId(userId: Long) {
        prefs.edit().putLong("ACTIVE_USER_ID", userId).apply()
    }

    fun getActiveUserId(): Long {
        return prefs.getLong("ACTIVE_USER_ID", -1L)
    }
}