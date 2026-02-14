package info.mobinaheidari.routerunner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main entry point for the RouteRunner application.
 *
 * This class is annotated with [@HiltAndroidApp], which triggers Hilt's code generation
 * and creates the application-level dependency container. It serves as the root of the
 * dependency injection graph, allowing Hilt to inject dependencies into other Android
 * components like Activities, Fragments, and Services.
 *
 * Note: This class must be registered in the AndroidManifest.xml via the `android:name` attribute.
 */
@HiltAndroidApp
class RouteRunnerApp : Application()