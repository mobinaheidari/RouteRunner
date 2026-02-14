package info.mobinaheidari.routerunner.presentation.login

/**
 * Represents the immutable UI state for the Login Screen.
 *
 * This data class models the complete state of the view at any given moment.
 * It follows the **Unidirectional Data Flow (UDF)** pattern:
 * - The ViewModel holds a single instance of this state.
 * - The UI observes changes to this state to update text fields, toggle visibility,
 * and display error messages or loading indicators.
 *
 * @property username The current text entered in the username field.
 * @property password The current text entered in the password field.
 * @property isPasswordVisible Controls whether the password text is masked (dots) or visible.
 * Toggled by the user via the eye icon.
 * @property isLoading Indicates if an authentication request is currently in progress.
 * Used to show a progress bar and disable the login button to prevent double-clicks.
 * @property error Holds any validation or authentication error messages (e.g., "Invalid password").
 * If null, no error is displayed.
 */
data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)