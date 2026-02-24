package info.mobinaheidari.routerunner.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.mobinaheidari.location.LocationClient        // <-- New Module
import info.mobinaheidari.location.DefaultLocationClient
import javax.inject.Singleton

/**
 * Dagger Module responsible for providing Location-related dependencies.
 *
 * Annotated with [@InstallIn(SingletonComponent::class)], this module ensures that
 * the provided dependencies live as long as the application itself.
 * This is crucial for location services, which often need to run independently of specific activities.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    /**
     * Provides a singleton instance of [LocationClient].
     *
     * This method acts as the **Binding** mechanism in Clean Architecture. It tells Hilt:
     * "Whenever a ViewModel asks for the [LocationClient] interface, give it an instance of [DefaultLocationClient]."
     *
     * @param context The Application Context, injected by Hilt. Using the Application Context
     * (instead of an Activity Context) prevents memory leaks when the service is running in the background.
     * @return The concrete implementation of the location client.
     */
    @Provides
    @Singleton // Ensures only one instance is created and shared across the app
    fun provideLocationClient(
        @ApplicationContext context: Context
    ): LocationClient {
        return DefaultLocationClient(context)
    }
}