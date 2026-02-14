package info.mobinaheidari.routerunner.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import info.mobinaheidari.routerunner.presentation.navigation.Screen
import java.io.File

/**
 * The main screen responsible for the map interface and tracking controls.
 *
 * This Composable integrates the Google Maps SDK to visualize the user's route.
 * It observes the [MapViewModel] state to:
 * 1. Draw a [Polyline] representing the recorded path.
 * 2. Animate the camera to follow the user during tracking.
 * 3. Manage the state of control buttons (Start, Stop, Share).
 *
 * @param navController Used for navigation (e.g., logging out).
 * @param userId The ID of the current user, used to load specific route data.
 * @param viewModel The state holder injected via Hilt.
 */
@SuppressLint("MissingPermission") // Permissions are requested in MainActivity before navigating here.
@Composable
fun MapScreen(
    navController: NavController,
    userId: Long,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Collect UI state from ViewModel (Reactive UI)
    val state by viewModel.state.collectAsState()

    // --- Map Configuration ---

    // Transformation: Convert DB Entities -> Google Maps LatLng objects
    val pathPoints = state.locations.map { LatLng(it.latitude, it.longitude) }

    // Client for "One-time" location checks (e.g., centering map on load)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Default Camera Position (Fallback: Tehran) if GPS is cold/disabled
    val defaultLocation = LatLng(35.6892, 51.3890)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // --- Side Effects (Lifecycle Handling) ---

    // 1. Initialization: Load data and check GPS settings when the screen opens.
    LaunchedEffect(userId) {
        viewModel.onEvent(MapEvent.LoadLocations(userId))
        checkAndEnableLocation(context)

        // Attempt to center camera on the user's last known location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 17f)
                }
            }
        }
    }

    // 2. Camera Tracking: Automatically follow the user when a new point is recorded.
    LaunchedEffect(state.locations) {
        if (state.locations.isNotEmpty() && state.isTracking) {
            val lastPoint = LatLng(state.locations.last().latitude, state.locations.last().longitude)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(lastPoint, 17f),
                durationMs = 1000 // Smooth animation over 1 second
            )
        }
    }

    // 3. Transient Feedback: Show Toasts for temporary errors/messages.
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // --- UI Layout ---
    Column(modifier = Modifier.fillMaxSize()) {

        // Region: Map View
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Enable "My Location" blue dot and system UI
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true,
                    compassEnabled = true
                )
            ) {
                // Draw the route path
                if (pathPoints.isNotEmpty()) {
                    Polyline(
                        points = pathPoints,
                        color = Color.Blue,
                        width = 15f
                    )
                }
            }
        }

        // Region: Control Panel (Bottom Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // State A: Idle (Ready to Start)
            if (!state.isTracking && !state.showDownloadButton) {
                Button(onClick = {
                    viewModel.onEvent(MapEvent.StartTracking(userId))
                }) { Text("Start Tracking") }
            }

            // State B: Tracking Active
            if (state.isTracking) {
                Button(
                    onClick = {
                        viewModel.onEvent(MapEvent.StopTracking)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Stop Tracking") }
            }

            // State C: Finished (Ready to Export)
            if (state.showDownloadButton) {
                Button(onClick = {
                    viewModel.onEvent(MapEvent.ExportCsv(context, userId) { file ->
                        // Trigger external share intent upon successful file generation
                        shareCsvFile(context, file)
                    })
                }) { Text("Share / Save CSV") }
            }

            // Logout Action (Always accessible)
            OutlinedButton(onClick = {
                viewModel.onEvent(MapEvent.Logout(context) {
                    navController.navigate(Screen.Welcome.route) { popUpTo(0) }
                })
            }) { Text("Logout") }
        }
    }
}

// --- Helper Functions ---

/**
 * Launches an Android System Intent to share the generated CSV file.
 *
 * Uses [FileProvider] to securely share the file with other apps (Gmail, Telegram, Drive, etc.).
 *
 * @param context Required to access the package manager and resources.
 * @param file The CSV file stored in the app's private storage.
 */
fun shareCsvFile(context: Context, file: File) {
    // Create a content URI for the file (requires <provider> in Manifest)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv" // MIME Type
        putExtra(Intent.EXTRA_STREAM, uri)
        // Grant temporary read permission to the receiving app
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Launch the chooser dialog
    context.startActivity(Intent.createChooser(shareIntent, "Share Route Data"))
}

/**
 * Checks if the device's Location Services (GPS) are enabled and configured for High Accuracy.
 *
 * If not, it triggers a system dialog ([ResolvableApiException]) prompting the user to enable them
 * without leaving the app.
 *
 * @param context The Activity context needed to launch the resolution dialog.
 */
fun checkAndEnableLocation(context: Context) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                // Show the "Turn on GPS" dialog
                val activity = context as? Activity
                activity?.let {
                    exception.startResolutionForResult(it, 1001)
                }
            } catch (sendEx: IntentSender.SendIntentException) {
                // Error sending intent, ignored for simplicity
            }
        }
    }
}