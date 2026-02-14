package info.mobinaheidari.routerunner.presentation.map

import android.content.Context
import java.io.File

/**
 * Represents the specific User Actions (Intents) that can occur on the Map Screen.
 *
 * In the **MVI architecture**, the UI (Compose) triggers these events, and the [MapViewModel]
 * processes them to update the [MapState] or perform side effects (like Service control).
 */
sealed class MapEvent {

    /**
     * Triggers the start of the location tracking service.
     *
     * Note: Unlike previous versions, this event is "clean" and does not require [Context],
     * as the ViewModel uses an injected [LocationClient] to handle the service interaction.
     *
     * @property userId The unique identifier of the user to associate location data with.
     */
    data class StartTracking(val userId: Long): MapEvent()

    /**
     * Signals the intent to stop the ongoing route recording.
     *
     * This stops the foreground service and finalizes the current tracking session.
     * Defined as an `object` because it carries no payload data.
     */
    object StopTracking: MapEvent()

    /**
     * Requests the initial loading of historical route data for the user.
     * Usually triggered when the screen enters the Composition (LaunchedEffect).
     */
    data class LoadLocations(val userId: Long): MapEvent()

    /**
     * Initiates the export of recorded location data to a CSV file.
     *
     * @property context Required here to access the device's internal file storage and
     * [FileProvider] for sharing. While ViewModels usually avoid Context, strictly
     * passing it as a method parameter for a specific "one-shot" operation is an acceptable trade-off.
     * @property onFileReady A callback lambda invoked when the file is successfully generated,
     * allowing the UI to trigger the system "Share" intent.
     */
    data class ExportCsv(
        val context: Context,
        val userId: Long,
        val onFileReady: (File) -> Unit
    ): MapEvent()

    /**
     * Handles the user logout process.
     *
     * Ensures that any active tracking services are strictly stopped before navigating away.
     *
     * @property context Used to perform cleanup tasks if necessary.
     * @property onLogout A callback to handle the actual navigation (e.g., popping back stack),
     * ensuring the ViewModel remains decoupled from the Navigation Controller.
     */
    data class Logout(
        val context: Context,
        val onLogout: () -> Unit
    ): MapEvent()
}