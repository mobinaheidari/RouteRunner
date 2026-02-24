package info.mobinaheidari.routerunner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the `locations` table in the local Room database.
 *
 * This entity acts as the fundamental unit of the route tracking feature.
 * Each row corresponds to a single GPS coordinate recorded at a specific moment in time.
 *
 * **Schema Design:**
 * - **Primary Key:** [id] ensures unique identification for every recorded point.
 * - **Foreign Key Logic:** The [userId] field establishes a logical relationship with the [UserEntity],
 * allowing the application to query routes specific to a logged-in user.
 *
 * @property id The auto-generated unique identifier for this record.
 * @property userId The ID of the user who generated this location point.
 * @property latitude The geographical latitude coordinate.
 * @property longitude The geographical longitude coordinate.
 * @property timestamp The epoch time (in milliseconds) when this location was recorded.
 * Used for chronological sorting and route reconstruction.
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)