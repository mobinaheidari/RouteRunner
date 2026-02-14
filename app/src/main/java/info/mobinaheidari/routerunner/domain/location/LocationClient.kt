package info.mobinaheidari.routerunner.domain.location

/**
 * Defines the contract for location tracking operations within the domain layer.
 *
 * This interface is a key part of **Clean Architecture**. It abstracts the details of
 * Android Components (like [Service], [Intent], and [Context]) from the Presentation layer.
 *
 * By using this interface, the ViewModel can trigger location tracking without knowing
 * *how* it is implemented (e.g., via a Foreground Service), making the code cleaner and easier to test.
 */
interface LocationClient {

    /**
     * Initiates the location tracking process.
     *
     * The implementation of this method is responsible for starting the
     * Android Foreground Service and passing the necessary User ID.
     *
     * @param userId The unique ID of the current user. This ensures that recorded
     * location points are associated with the correct account in the database.
     */
    fun startLocationTracking(userId: Long)

    /**
     * Stops the location tracking process.
     *
     * This method ensures that the Foreground Service is stopped, the notification
     * is removed, and battery resources are released.
     */
    fun stopLocationTracking()


}