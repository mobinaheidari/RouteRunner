package info.mobinaheidari.routerunner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The main database configuration class for the application.
 *
 * This abstract class serves as the primary access point to the persisted data.
 * It uses the **Room Persistence Library** to provide an abstraction layer over SQLite.
 *
 * **Database Configuration:**
 * - **Entities:** Includes [UserEntity] (users table) and [LocationEntity] (locations table).
 * - **Version:** 1 (Initial schema).
 * - **Export Schema:** defaults to true (useful for version control tracking of schema changes).
 *
 * @see AppDao for the available database operations.
 */
@Database(
    entities = [UserEntity::class, LocationEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the Data Access Object (DAO).
     *
     * Room will automatically generate the implementation of this method at compile time.
     * Use this DAO to perform all CRUD (Create, Read, Update, Delete) operations on the database.
     *
     * @return The [AppDao] interface for database interactions.
     */
    abstract fun dao(): AppDao
}