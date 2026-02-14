package info.mobinaheidari.routerunner.presentation.login

/**
 * Represents all possible UI events (user actions) in the Login Screen.
 *
 * This sealed class is a core component of the **MVI (Model-View-Intent)** architecture.
 * The UI (Composable) triggers these events, and the [LoginViewModel] processes them
 * to update the [LoginState] or perform side effects like authentication.
 */
sealed class LoginEvent {

    /**
     * Triggered when the user types in the "Username" text field.
     * @property value The current text input.
     */
    data class UsernameChanged(val value: String): LoginEvent()

    /**
     * Triggered when the user types in the "Password" text field.
     * @property value The current text input.
     */
    data class PasswordChanged(val value: String): LoginEvent()

    /**
     * Triggered when the user clicks the "eye" icon to show/hide the password.
     * Defined as an `object` because it carries no payload data; it simply toggles a boolean state.
     */
    object TogglePasswordVisibility: LoginEvent()

    /**
     * Triggered when the user clicks the "Login" button.
     *
     * @property onLoginSuccess A callback lambda to be invoked only if authentication
     * is successful. This allows the ViewModel to trigger navigation (side effect)
     * without holding a reference to the [NavController], keeping the architecture clean.
     */
    data class LoginClicked(val onLoginSuccess: (Long) -> Unit): LoginEvent()
}