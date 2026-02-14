package info.mobinaheidari.routerunner.presentation.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * The UI screen for new user registration.
 *
 * This composable provides a scrollable form for collecting user details (Name, Age, Date of Birth, Credentials).
 * It observes the [RegisterViewModel] state to reactively update the UI, display validation errors,
 * and handle navigation events upon successful account creation.
 *
 * Key Features:
 * - **Form Validation:** Visual feedback (red outlines) when fields are invalid after submission.
 * - **Visual Transformation:** Auto-formatting for the birth date field.
 * - **State Handling:** Displays a loading indicator while the database operation is in progress.
 *
 * @param navController Manages navigation between screens (e.g., redirecting to Login or going back).
 * @param viewModel The state holder, injected via Hilt, containing form logic and database operations.
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .widthIn(max = 400.dp) // Optimizes layout for tablets/wide screens
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // region Input Fields

                // --- First Name ---
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onEvent(RegisterEvent.FirstNameChanged(it)) },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.firstName.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- Last Name ---
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onEvent(RegisterEvent.LastNameChanged(it)) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.lastName.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- Age ---
                OutlinedTextField(
                    value = state.age,
                    onValueChange = { viewModel.onEvent(RegisterEvent.AgeChanged(it)) },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.age.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- Birth Date ---
                // Applies DateTransformation to visually format inputs like "13780520" to "1378-05-20"
                OutlinedTextField(
                    value = state.birthDate,
                    onValueChange = { viewModel.onEvent(RegisterEvent.BirthDateChanged(it)) },
                    label = { Text("Birth Date (YYYYMMDD)") },
                    placeholder = { Text("e.g. 13780520") },
                    visualTransformation = DateTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.birthDate.length != 8
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- Username ---
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.onEvent(RegisterEvent.UsernameChanged(it)) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.username.length < 4
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- Password ---
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.isSubmitted && state.password.length < 6
                )

                // endregion

                // region Error Display & Actions

                // --- Error Message ---
                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Submit Button ---
                Button(
                    onClick = {
                        viewModel.onEvent(RegisterEvent.SubmitClicked {
                            Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                            // Navigate to login, popping the 'welcome' screen to prevent back-navigation loops
                            navController.navigate("login") {
                                popUpTo("welcome") { inclusive = false }
                            }
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sign Up")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link to Login Page
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Already have an account? Login")
                }

                // endregion
            }
        }
    }
}

/**
 * A custom [VisualTransformation] to format raw date input (YYYYMMDD) into a readable date string (YYYY-MM-DD).
 *
 * This transformation does not change the underlying state (which remains an 8-digit string),
 * but modifies how it is displayed to the user. It also handles **cursor positioning (OffsetMapping)**
 * so that the cursor behaves naturally when skipping over the inserted hyphens.
 *
 * Example: Input "13780520" -> Display "1378-05-20"
 */
class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Limit display to 8 characters to prevent overflow in formatting
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""

        // Insert hyphens at specific indices (YYYY-MM-DD)
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 5) out += "-"
        }

        // Define mapping between original text indices and transformed text indices
        val dateOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 5) return offset + 1 // Jump over first hyphen
                if (offset <= 8) return offset + 2 // Jump over second hyphen
                return 10 // Max length
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 10) return offset - 2
                return 8 // Max length
            }
        }
        return TransformedText(AnnotatedString(out), dateOffsetMapping)
    }
}