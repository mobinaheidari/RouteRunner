package info.mobinaheidari.routerunner.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mobinaheidari.routerunner.data.local.AppDao
import info.mobinaheidari.routerunner.data.local.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state and business logic of the Registration Screen.
 *
 * This class handles:
 * 1. **Input Management:** Updates the UI state as the user types (Two-way binding equivalent).
 * 2. **Input Validation:** Enforces rules for required fields, date formats, and password strength.
 * 3. **Data Persistence:** interacts with [AppDao] to save the new user to the local database.
 * 4. **Error Handling:** Manages duplicate username conflicts and other database errors.
 *
 * @property dao The Data Access Object injected by Hilt for database operations.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val dao: AppDao
) : ViewModel() {

    // Backing property for mutable state
    private val _state = MutableStateFlow(RegisterState())


    /**
     * Exposes the current registration state as a read-only [StateFlow] to the UI.
     * The UI observes this to update text fields and show/hide error messages.
     */
    val state = _state.asStateFlow()

    /**
     * Processes user interactions (events) triggered from the UI layer.
     *
     * @param event The specific [RegisterEvent] to handle (e.g., text changes, submit button click).
     */
    fun onEvent(event: RegisterEvent) {
        when(event) {
            is RegisterEvent.FirstNameChanged -> {
                _state.update { it.copy(firstName = event.value, errorMessage = null) }
            }
            is RegisterEvent.LastNameChanged -> {
                _state.update { it.copy(lastName = event.value, errorMessage = null) }
            }
            is RegisterEvent.AgeChanged -> {
                // strict input filter: Ensure only numeric digits are entered
                if (event.value.all { it.isDigit() }) {
                    _state.update { it.copy(age = event.value, errorMessage = null) }
                }
            }
            is RegisterEvent.BirthDateChanged -> {
                // Input mask logic: Limit to 8 digits (YYYYMMDD)
                if (event.value.length <= 8 && event.value.all { it.isDigit() }) {
                    _state.update { it.copy(birthDate = event.value, errorMessage = null) }
                }
            }
            is RegisterEvent.UsernameChanged -> {
                _state.update { it.copy(username = event.value, errorMessage = null) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.value, errorMessage = null) }
            }
            is RegisterEvent.SubmitClicked -> {
                performRegister(event.onRegisterSuccess)
            }
        }
    }

    /**
     * Validates the current input state and attempts to register the user in the database.
     *
     * This method performs the following steps:
     * 1. **Validation:** Checks for empty fields, correct date format (8 digits), and minimum lengths.
     * 2. **Formatting:** Transforms the raw date string (e.g., "13800101") to a standard format ("1380-01-01").
     * 3. **Persistence:** Calls [AppDao.registerUser] within a coroutine.
     * 4. **Conflict Handling:** catches exceptions (like Unique Constraint violations) if the username exists.
     *
     * @param onSuccess Callback lambda to be invoked if registration is successful (navigates to Login).
     */
    private fun performRegister(onSuccess: () -> Unit) {
        // Mark as submitted to trigger inline error visibility in the UI
        _state.update { it.copy(isSubmitted = true) }
        val currentState = _state.value

        // --- 1. Validation Logic ---
        val datePattern = Regex("^\\d{8}$")
        val error = when {
            currentState.firstName.isBlank() -> "First Name is required"
            currentState.lastName.isBlank() -> "Last Name is required"
            currentState.age.isBlank() -> "Age is required"
            !datePattern.matches(currentState.birthDate) -> "Birth Date must be 8 digits (e.g. 13800101)"
            currentState.username.length < 4 -> "Username must be at least 4 chars"
            currentState.password.length < 6 -> "Password must be at least 6 chars"
            else -> null
        }

        if (error != null) {
            _state.update { it.copy(errorMessage = error) }
            return
        }

        // --- 2. Registration Logic (Database Interaction) ---
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Data Transformation: 13780520 -> 1378-05-20
                val rawDate = currentState.birthDate
                val formattedDate = "${rawDate.substring(0,4)}-${rawDate.substring(4,6)}-${rawDate.substring(6,8)}"

                val newUser = UserEntity(
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    age = currentState.age.toIntOrNull() ?: 0,
                    birthDate = formattedDate,
                    username = currentState.username,
                    password = currentState.password
                )

                // Attempt to insert. If conflict strategy is ABORT, this throws an exception on duplicate username.
                dao.registerUser(newUser)

                _state.update { it.copy(isLoading = false) }
                onSuccess()

            } catch (e: Exception) {
                // Handle Unique Constraint Violation (Duplicate Username)
                _state.update {
                    it.copy(isLoading = false, errorMessage = "User already exists!")
                }
            }
        }
    }
}