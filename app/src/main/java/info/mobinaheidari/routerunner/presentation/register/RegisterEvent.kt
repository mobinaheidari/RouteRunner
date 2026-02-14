package info.mobinaheidari.routerunner.presentation.register

/**
 * Represents all possible UI events (user actions) in the Registration Screen.
 *
 * This sealed class is part of the **Unidirectional Data Flow (UDF)** architecture.
 * The UI (Composable) triggers these events, and the [RegisterViewModel] processes them
 * to produce a new [RegisterState].
 */
sealed class RegisterEvent {

    /**
     * Triggered when the user types in the "First Name" text field.
     * @property value The current text input.
     */
    data class FirstNameChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user types in the "Last Name" text field.
     * @property value The current text input.
     */
    data class LastNameChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user types in the "Age" text field.
     * @property value The raw input string (validated for digits in the ViewModel).
     */
    data class AgeChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user types in the "Birth Date" text field.
     * @property value The raw input string (e.g., "13780520").
     */
    data class BirthDateChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user types in the "Username" text field.
     * @property value The current text input.
     */
    data class UsernameChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user types in the "Password" text field.
     * @property value The current text input.
     */
    data class PasswordChanged(val value: String): RegisterEvent()

    /**
     * Triggered when the user clicks the "Sign Up" button.
     *
     * @property onRegisterSuccess A callback lambda to be invoked only if registration
     * is successful (e.g., to navigate to the Login screen). This keeps navigation logic
     * decoupled from the ViewModel.
     */
    data class SubmitClicked(val onRegisterSuccess: () -> Unit): RegisterEvent()
}