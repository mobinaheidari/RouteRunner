package info.mobinaheidari.routerunner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import info.mobinaheidari.routerunner.presentation.login.LoginScreen
import info.mobinaheidari.routerunner.presentation.map.MapScreen
import info.mobinaheidari.routerunner.presentation.navigation.Screen
import info.mobinaheidari.routerunner.presentation.register.RegisterScreen
import info.mobinaheidari.routerunner.presentation.welcome.WelcomeScreen

/**
 * The single Activity for the application, serving as the entry point for the UI.
 *
 * Annotated with [@AndroidEntryPoint] to enable Hilt dependency injection.
 * It hosts the [RouteRunnerNavHost] which manages the application's navigation and permission logic.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RouteRunnerNavHost()
        }
    }
}

/**
 * The root Composable function responsible for orchestrating the application's navigation graph
 * and handling mandatory runtime permissions.
 *
 * It acts as a gatekeeper:
 * 1. Checks if critical permissions (Location & Notifications) are granted.
 * 2. If denied, displays a [PermissionRequestScreen].
 * 3. If granted, initializes the [NavHost] and allows access to the app features.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteRunnerNavHost() {
    val navController = rememberNavController()

    // 1. Define the list of mandatory permissions required for core functionality.
    // ACCESS_FINE_LOCATION: For precise GPS tracking.
    // ACCESS_COARSE_LOCATION: Required by API 31+ when requesting fine location.
    // POST_NOTIFICATIONS: Required by API 33+ for Foreground Service notifications.
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )

    // 2. Check the current permission status.
    if (!permissionsState.allPermissionsGranted) {
        // Fallback UI: If critical permissions are missing, block access and request them.
        PermissionRequestScreen(
            onGrantClick = { permissionsState.launchMultiplePermissionRequest() }
        )
    } else {
        // Main UI: Permissions are granted; set up the Navigation Graph.
        NavHost(navController = navController, startDestination = Screen.Welcome.route) {

            // Welcome / Landing Screen
            composable(Screen.Welcome.route) {
                WelcomeScreen(navController)
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }

            // Registration Screen
            composable(Screen.Register.route) {
                RegisterScreen(navController)
            }

            // Map / Tracking Screen
            // Accepts a 'userId' argument to load specific user data.
            composable(
                route = Screen.Map.route,
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                MapScreen(navController, userId)
            }
        }
    }
}

/**
 * A user interface component displayed when required permissions are denied.
 *
 * It provides context to the user explaining *why* the permissions are needed
 * and offers a button to trigger the system permission request dialog.
 *
 * @param onGrantClick Callback lambda to be invoked when the user clicks the "Grant" button.
 */
@Composable
fun PermissionRequestScreen(onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To track your run and display the map, RouteRunner needs access to your location.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrantClick) {
            Text("Grant Permissions")
        }
    }
}