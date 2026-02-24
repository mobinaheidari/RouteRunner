package info.mobinaheidari.routerunner.presentation.map


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import info.mobinaheidari.routerunner.data.local.AppDao // For your local DB
import info.mobinaheidari.location.LocationClient
import info.mobinaheidari.routerunner.data.local.SessionManager

/**
 * ViewModel responsible for managing the state and business logic of the Map Screen.
 *
 * This class acts as a bridge between the UI, the local database, and the background location service.
 * It handles:
 * 1. **Real-time Tracking:** communicating with [LocationClient] to start/stop the foreground service.
 * 2. **Data Observation:** Listening to database changes via Flow to update the map path dynamically.
 * 3. **Data Export:** Converting recorded location points into a CSV file for sharing.
 *
 * @property dao The Data Access Object for reading/writing location data.
 * @property locationClient An abstraction for controlling the Android Foreground Service.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val dao: AppDao,
    private val locationClient: LocationClient,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Backing property for the UI state
    private val _state = MutableStateFlow(MapState())

    /**
     * Exposes the current state of the map screen (path points, tracking status, etc.)
     * as a read-only [StateFlow] for the UI to observe.
     */
    val state = _state.asStateFlow()

    /**
     * Processes user interactions (events) from the Map Screen.
     *
     * @param event The specific action triggered by the user (e.g., Start Tracking, Export).
     */
    fun onEvent(event: MapEvent) {
        when(event) {
            is MapEvent.LoadLocations -> {
                observeLocations(event.userId)
            }

            is MapEvent.StartTracking -> {
                // 🟢 Save the user as the "Active" user before starting the service
                sessionManager.saveActiveUserId(event.userId)
                // Start the clean service (No ID passed!)
                locationClient.startLocationTracking()
                _state.update { it.copy(isTracking = true, showDownloadButton = false) }
            }

            is MapEvent.StopTracking -> {
                // Stop the background service
                locationClient.stopLocationTracking()

                // Update UI: Toggle tracking flag and show download button
                _state.update { it.copy(isTracking = false, showDownloadButton = true) }
            }

            is MapEvent.ExportCsv -> {
                // Logic to generate the CSV file
                val file = generateCsvFile(event.context)
                if (file != null) {
                    // Trigger the callback to share the file via Intent
                    event.onFileReady(file)
                } else {
                    _state.update { it.copy(errorMessage = "No location data found to export.") }
                }
            }

            is MapEvent.Logout -> {
                // Safety: Ensure tracking stops when logging out to prevent zombie services
                locationClient.stopLocationTracking()
                event.onLogout()
            }
        }
    }

    /**
     * Observes the database for location updates for a specific user.
     *
     * Because [AppDao.getUserLocations] returns a Flow, this block will stay active
     * and automatically emit new lists whenever the database changes (e.g., when the Service inserts a new point).
     */
    private fun observeLocations(userId: Long) {
        viewModelScope.launch {
            dao.getUserLocations(userId).collect { list ->
                _state.update {
                    it.copy(locations = list)
                }
            }
        }
    }

    /**
     * Generates a CSV file containing the route data.
     *
     * The file is saved in the app's private external storage directory.
     * Format: Latitude, Longitude, Timestamp, Readable Date
     *
     * @param context Used to access the application's file directory.
     * @return A [File] reference to the generated CSV, or null if the list is empty.
     */
    private fun generateCsvFile(context: Context): File? {
        val list = _state.value.locations
        if (list.isEmpty()) return null

        // 1. Create Header
        val header = "Latitude,Longitude,Timestamp,Date Time\n"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // 2. Map data to CSV rows
        val rows = list.joinToString("\n") { location ->
            val readableDate = sdf.format(Date(location.timestamp))
            "${location.latitude},${location.longitude},${location.timestamp},$readableDate"
        }

        val content = header + rows

        // 3. Save to app-specific external storage (does not require write permissions)
        val fileName = "route_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        file.writeText(content)
        return file
    }
}