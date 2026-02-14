package info.mobinaheidari.routerunner.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * The initial landing screen of the RouteRunner application.
 *
 * This composable serves as the entry point for unauthenticated users. It displays the
 * application branding (Logo & Title) and provides two primary actions:
 * 1. **Login:** For existing users to access their account.
 * 2. **Register:** For new users to create an account.
 *
 * @param navController Used to navigate to the Login or Register screens.
 * @param viewModel The [WelcomeViewModel] injected via Hilt, responsible for any
 * initialization logic (e.g., checking auto-login status).
 */
@Composable
fun WelcomeScreen(
    navController: NavController,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .widthIn(max = 400.dp), // Ensures UI looks good on tablets/wide screens
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // region Branding Section

            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "RouteRunner Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "RouteRunner",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))



            // Primary Action: Navigate to Login
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", fontSize = 18.sp)
            }

            // Secondary Action: Navigate to Registration
            OutlinedButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Account", fontSize = 18.sp)
            }


        }
    }
}