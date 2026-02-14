package info.mobinaheidari.routerunner.presentation.map

import info.mobinaheidari.routerunner.data.local.LocationEntity

/**
 * Represents the immutable UI state for the Map Screen.
 *
 * This data class is used to model the current snapshot of the screen, following the
 * **Unidirectional Data Flow (UDF)** pattern. The [MapViewModel] updates this state,
 * and the [MapScreen] observes it to render the UI components (e.g., polyline, buttons).
 *
 * @property locations A list of recorded [LocationEntity] points. Used to draw the route
 * (polyline) on the map. It updates in real-time as the user moves.
 * @property isTracking Indicates whether the location tracking service is currently active.
 * Used to toggle between "Start" and "Stop" buttons.
 * @property showDownloadButton Controls the visibility of the "Export CSV" button.
 * typically becomes true only after tracking has stopped and data is available.
 * @property errorMessage Holds transient error messages (e.g., "GPS signal lost" or "Export failed").
 * If not null, the UI should display a Toast or Snackbar and then clear this field.
 */
data class MapState(
    val locations: List<LocationEntity> = emptyList(),
    val isTracking: Boolean = false,
    val showDownloadButton: Boolean = false,
    val errorMessage: String? = null
)