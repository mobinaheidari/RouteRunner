package info.mobinaheidari.routerunner.di

import android.app.Application
import androidx.room.Room
import info.mobinaheidari.routerunner.data.local.AppDao
import info.mobinaheidari.routerunner.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Module responsible for providing application-wide dependencies, specifically for data persistence.
 *
 * This module is installed in the [SingletonComponent], meaning all provided dependencies
 * will have a lifecycle that matches the entire application duration. This is essential for
 * expensive objects like database instances, which should not be recreated frequently.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the singleton instance of the [AppDatabase].
     *
     * This method builds the Room database using the application context.
     *
     * @param app The [Application] context provided by Hilt, ensuring the database is tied
     * to the app lifecycle and not a specific activity.
     * @return The built [AppDatabase] instance.
     */
    @Provides
    @Singleton // Ensures only one database instance exists (Singleton pattern)
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "routerunner_db"
        ).build()
    }

    /**
     * Provides the [AppDao] instance for dependency injection.
     *
     * Instead of manually calling `db.dao()` in every ViewModel, Hilt uses this method
     * to inject the DAO directly. This promotes loose coupling, as ViewModels depend
     * only on the DAO interface, not the database implementation.
     *
     * @param db The [AppDatabase] instance provided by the [provideDatabase] method above.
     * @return The Data Access Object for user and location operations.
     */
    @Provides
    @Singleton
    fun provideDao(db: AppDatabase): AppDao {
        return db.dao()
    }
}