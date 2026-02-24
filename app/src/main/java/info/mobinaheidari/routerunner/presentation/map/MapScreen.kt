package info.mobinaheidari.routerunner.presentation.map

// 🟢 NEW: Import the helper from your location module
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import info.mobinaheidari.location.GeoJsonHelper
import info.mobinaheidari.routerunner.data.local.LocationEntity
import info.mobinaheidari.routerunner.presentation.navigation.Screen
import java.io.File

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: NavController,
    userId: Long,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // 🟢 1. LOAD POLYGON: Fetch coordinates from the helper in the :location module
    val polygonPoints by produceState<List<LatLng>>(initialValue = emptyList()) {
        // This runs in a background thread automatically
        value = GeoJsonHelper.getPolygonCoordinates(context)
    }

    // 🟢 2. SEGMENTATION LOGIC:
    // Instead of mapping to a single list of points, we split them into segments.
    // This ensures that if the user goes OUTSIDE (recording stops), we don't draw a
    // straight line across the forbidden zone when they come back IN.
    val pathSegments = remember(state.locations) {
        createDiscontinuousSegments(state.locations)
    }

    // 📍 CALCULATE CURRENT LOCATION
    val lastPoint = state.locations.lastOrNull()
    val currentLatLng = if (lastPoint != null) {
        LatLng(lastPoint.latitude, lastPoint.longitude)
    } else {
        LatLng(35.722, 51.385) // Default center (Tehran)
    }

    // 🔵 RESTORED LOGIC: Initialize FusedLocationClient for the "First Launch" check
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Initialize Camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }

    // 1. Initialization & Initial Center
    LaunchedEffect(userId) {
        viewModel.onEvent(MapEvent.LoadLocations(userId))
        checkAndEnableLocation(context)

        // 🔵 RESTORED LOGIC: If DB is empty, try to get real GPS location instead of default
        if (state.locations.isEmpty() &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude), 15f
                    )
                }
            }
        }
    }

    // 2. Camera Tracking (Auto-follow)
    LaunchedEffect(currentLatLng) {
        if (state.isTracking && lastPoint != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(currentLatLng),
                durationMs = 1000
            )
        }
    }

    // 3. Error Handling
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Disable Google's Blue Dot (we draw our own)
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = true,
                    compassEnabled = true
                )
            ) {
                // 🟢 DRAW POLYGON (The Allowed Zone)
                if (polygonPoints.isNotEmpty()) {
                    Polygon(
                        points = polygonPoints,
                        fillColor = Color.Blue.copy(alpha = 0.15f), // Transparent Blue
                        strokeColor = Color.Blue,
                        strokeWidth = 3f
                    )
                }

                // 🟢 DRAW PATH SEGMENTS
                // We iterate through the segments. If the path is continuous, this loop runs once.
                // If there are gaps (user went outside), it draws multiple separate lines.
                pathSegments.forEach { segment ->
                    Polyline(
                        points = segment,
                        color = Color.Red,
                        width = 5f,
                        zIndex = 10f // 🟢 ADD THIS: Forces the line to be drawn ON TOP of the polygon
                    )
                }

                // Manual Marker (Your custom "Blue Dot")
                if (lastPoint != null) {
                    Marker(
                        state = MarkerState(position = currentLatLng),
                        title = "Current Location",
                        snippet = "Lat: ${lastPoint.latitude}, Lng: ${lastPoint.longitude}"
                    )
                }
            }
            Text(
                text = "Points: ${state.locations.size}\nSegments: ${pathSegments.size}",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp) // Move down so it doesn't overlap buttons
                    .background(Color.White)
                    .padding(8.dp),
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Region: Control Panel (Kept exactly as original)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // State A: Idle
            if (!state.isTracking && !state.showDownloadButton) {
                // Inside MapScreen Composable Button
                Button(onClick = {
                    viewModel.onEvent(MapEvent.StartTracking(userId)) // 🟢 Use 'userId' from MapScreen params
                }) { Text("Start Tracking") }
            }

            // State B: Tracking
            if (state.isTracking) {
                Button(
                    onClick = {
                        viewModel.onEvent(MapEvent.StopTracking)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Stop Tracking") }
            }

            // State C: Finished
            if (state.showDownloadButton) {
                Button(onClick = {
                    viewModel.onEvent(MapEvent.ExportCsv(context, userId) { file ->
                        shareCsvFile(context, file)
                    })
                }) { Text("Share / Save CSV") }
            }

            // Logout
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
 * 🟢 NEW HELPER: Splits the path into segments based on time gaps.
 * If the gap between two points is > 20 seconds, we assume recording paused (user outside).
 */
fun createDiscontinuousSegments(locations: List<LocationEntity>): List<List<LatLng>> {
    if (locations.isEmpty()) return emptyList()

    val segments = mutableListOf<List<LatLng>>()
    var currentSegment = mutableListOf<LatLng>()

    // Sort to ensure time order
    val sortedLocs = locations.sortedBy { it.timestamp }

    if (sortedLocs.isNotEmpty()) {
        currentSegment.add(LatLng(sortedLocs[0].latitude, sortedLocs[0].longitude))
    }

    for (i in 0 until sortedLocs.size - 1) {
        val p1 = sortedLocs[i]
        val p2 = sortedLocs[i+1]

        // Time difference check (20 seconds = 20,000 ms)
        // Adjust this threshold if your update interval changes.
        val timeDiff = p2.timestamp - p1.timestamp

        if (timeDiff > 30_000) {
            segments.add(currentSegment)
            currentSegment = mutableListOf()
        }
        currentSegment.add(LatLng(p2.latitude, p2.longitude))
    }

    if (currentSegment.isNotEmpty()) {
        segments.add(currentSegment)
    }

    return segments
}

// --- Original Helpers (Preserved) ---

fun shareCsvFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Route Data"))
}

fun checkAndEnableLocation(context: Context) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
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