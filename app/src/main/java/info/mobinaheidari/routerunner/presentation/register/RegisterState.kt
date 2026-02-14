package info.mobinaheidari.routerunner.presentation.register

/**
 * Represents the immutable UI state for the Registration Screen.
 *
 * This data class follows the **Unidirectional Data Flow (UDF)** pattern.
 * The ViewModel maintains a single instance of this state, and the UI observes it
 * to render text fields, loading indicators, and error messages.
 *
 * @property age Kept as [String] to allow raw input handling and prevent [NumberFormatException]
 * while the user is typing. It is validated and converted to [Int] only upon submission.
 * @property birthDate Raw input string (e.g., "13780520") which is formatted (to "YYYY-MM-DD")
 * before being sent to the database.
 * @property isLoading Indicates if a background operation (database insertion) is in progress.
 * @property errorMessage Holds validation errors or database conflict messages to be displayed via SnackBar or text.
 * @property isSubmitted A flag used to control **Validation Feedback**. It ensures that validation errors
 * (red text) are shown only *after* the user attempts to submit the form, improving UX.
 */
data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val age: String = "",
    val birthDate: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
)