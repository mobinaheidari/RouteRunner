package info.mobinaheidari.routerunner.presentation.navigation

/**
 * Encapsulates the navigation hierarchy and route definitions for the RouteRunner application.
 *
 * This sealed class ensures **Type-Safe Navigation** by centralizing route strings.
 * It prevents hardcoded string errors and simplifies argument handling across the app.
 *
 * Usage:
 * - Simple screens: use [Screen.Welcome.route]
 * - Parameterized screens: use [Screen.Map.createRoute]
 */
sealed class Screen(val route: String) {

    /**
     * The initial landing screen shown to unauthenticated users.
     * Route: "welcome"
     */
    object Welcome : Screen("welcome")

    /**
     * The login screen for existing users.
     * Route: "login"
     */
    object Login : Screen("login")

    /**
     * The registration screen for creating a new account.
     * Route: "register"
     */
    object Register : Screen("register")

    /**
     * The main tracking screen where the user's route is displayed.
     * This route requires a dynamic argument: [userId].
     *
     * Route Pattern: "map/{userId}"
     */
    object Map : Screen("map/{userId}") {

        /**
         * Helper function to build the navigation route with the required argument.
         *
         * Use this method when navigating *to* the map screen to ensure the route is formatted correctly.
         *
         * @param userId The unique ID of the logged-in user.
         * @return A valid route string, e.g., "map/1001"
         */
        fun createRoute(userId: Long) = "map/$userId"
    }
}