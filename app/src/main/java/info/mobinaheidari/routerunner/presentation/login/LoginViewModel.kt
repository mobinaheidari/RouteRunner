package info.mobinaheidari.routerunner.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mobinaheidari.routerunner.data.local.AppDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the business logic and state of the Login Screen.
 *
 * This class handles:
 * 1. **Input State Management:** Storing the username, password, and visibility toggle.
 * 2. **Authentication Logic:** Validating inputs and querying the [AppDao] to verify credentials.
 * 3. **UI Feedback:** exposing loading states and error messages to the view.
 *
 * @property dao The Data Access Object used to perform database queries (finding the user).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dao: AppDao
) : ViewModel() {

    // Backing property for mutable state, accessible only within this ViewModel.
    private val _state = MutableStateFlow(LoginState())

    /**
     * Exposes the current login state as a read-only [StateFlow].
     * The UI observes this flow to reactively update the screen (e.g., show error text).
     */
    val state = _state.asStateFlow()

    /**
     * Processes user interactions (events) triggered from the Login Screen.
     *
     * @param event The specific [LoginEvent] to handle (e.g., typing text, clicking login).
     */
    fun onEvent(event: LoginEvent) {
        when(event) {
            is LoginEvent.UsernameChanged -> {
                // Update username and clear any previous error messages for better UX
                _state.update { it.copy(username = event.value, error = null) }
            }
            is LoginEvent.PasswordChanged -> {
                // Update password and clear errors
                _state.update { it.copy(password = event.value, error = null) }
            }
            is LoginEvent.TogglePasswordVisibility -> {
                // Toggle the boolean flag for password masking
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginEvent.LoginClicked -> {
                performLogin(event.onLoginSuccess)
            }
        }
    }

    /**
     * Executes the login operation asynchronously.
     *
     * Steps:
     * 1. **Validation:** Checks if fields are empty.
     * 2. **Database Query:** Runs a suspend function on [viewModelScope] to find the user.
     * 3. **Result Handling:**
     * - If user exists -> Triggers [onSuccess] callback (Navigate to Map).
     * - If user is null -> Shows "Invalid credentials" error.
     * - If exception -> Shows generic system error.
     *
     * @param onSuccess Callback lambda to navigate the user upon successful authentication.
     */
    private fun performLogin(onSuccess: (Long) -> Unit) {
        val currentState = _state.value

        // 1. Client-side Validation (Fail fast)
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            // Show loading indicator
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Attempt to retrieve user from DB
                // Note: returns null if no user matches (requires 'UserEntity?' in Dao)
                val user = dao.loginUser(currentState.username, currentState.password)

                // Hide loading indicator
                _state.update { it.copy(isLoading = false) }

                if (user != null) {
                    // ✅ Success: Pass the User ID to the navigation logic
                    onSuccess(user.id)
                } else {
                    // ❌ Failure: Credential mismatch
                    _state.update {
                        it.copy(error = "Invalid username or password")
                    }
                }

            } catch (e: Exception) {
                // 🛑 Exception Handling: Catches unexpected database errors (e.g., IO issues)
                e.printStackTrace()
                _state.update {
                    // Show a user-friendly message instead of the raw exception
                    it.copy(isLoading = false, error = "Something went wrong. Please try again.")
                }
            }
        }
    }
}