package info.mobinaheidari.routerunner.presentation.welcome

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state and business logic of the [WelcomeScreen].
 *
 * This class serves as the architectural logic holder for the application's landing page.
 * While currently minimal, it is designed to extend future capabilities such as:
 * - **Session Management:** Checking if a user is already logged in to perform auto-navigation.
 * - **UI State:** Managing dynamic content on the welcome screen if needed.
 * - **Navigation Events:** Handling logic before navigating to Login or Register screens.
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    // Future dependency injection:
    // private val authRepository: AuthRepository
) : ViewModel() {

    // Placeholder for future logic:
    // fun checkUserSession() { ... }
}