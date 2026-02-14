package info.mobinaheidari.routerunner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for database operations.
 *
 * This interface defines the contract for all **SQL queries** executed against the application database.
 * Room automatically generates the implementation at compile time, ensuring type safety and optimized SQL.
 *
 * **Key Functionalities:**
 * - **User Management:** Registration and Login.
 * - **Location Tracking:** Recording GPS coordinates and querying route history.
 */
@Dao
interface AppDao {

    // region User Operations

    /**
     * Inserts a new user record into the database.
     *
     * @param user The [UserEntity] object to be inserted.
     * @return The row ID of the newly inserted user.
     * @throws SQLiteConstraintException If a user with the same [UserEntity.username] already exists
     * (due to [OnConflictStrategy.ABORT]).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity): Long

    /**
     * Authenticates a user by checking credentials.
     *
     * Queries the database for a matching username and password combination.
     *
     * @param user The entered username.
     * @param pass The entered password.
     * @return The matching [UserEntity] if found, or `null` if no match exists.
     */
    @Query("SELECT * FROM users WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun loginUser(user: String, pass: String): UserEntity?

    // endregion

    // region Location Operations

    /**
     * Records a new location point in the database.
     *
     * Typically called by the background service whenever a location update is received.
     *
     * @param location The [LocationEntity] object containing latitude, longitude, and timestamp.
     */
    @Insert
    suspend fun insertLocation(location: LocationEntity)

    /**
     * Retrieves the complete history of location points for a specific user.
     *
     * The results are ordered by timestamp (ASC) to ensure the route is drawn sequentially.
     *
     * **Reactive Return Type:**
     * Returns a [Flow], which means the UI will automatically receive updates whenever
     * a new location is inserted into the database.
     *
     * @param userId The ID of the user whose route is being queried.
     * @return A stream of location lists.
     */
    @Query("SELECT * FROM locations WHERE userId = :userId ORDER BY timestamp ASC")
    fun getUserLocations(userId: Long): Flow<List<LocationEntity>>

    /**
     * Deletes all location history for a specific user.
     *
     * Useful for resetting the route or cleaning up data upon logout/account deletion.
     *
     * @param userId The ID of the user whose location data should be cleared.
     */
    @Query("DELETE FROM locations WHERE userId = :userId")
    suspend fun clearUserLocations(userId: Long)

    // endregion
}