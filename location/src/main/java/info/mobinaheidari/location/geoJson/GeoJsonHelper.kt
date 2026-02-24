package info.mobinaheidari.location

import android.content.Context
import android.util.JsonReader // 🟢 Standard Android Streaming Parser
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object GeoJsonHelper {
    private var cachedPolygon: List<LatLng>? = null

    // 🟢 Suspend function to force background execution
    suspend fun getPolygonCoordinates(context: Context): List<LatLng> = withContext(Dispatchers.IO) {
        if (cachedPolygon != null) return@withContext cachedPolygon!!

        val coordinates = mutableListOf<LatLng>()

        try {
            val inputStream = context.resources.openRawResource(R.raw.polygon)
            val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))

            // 🟢 DOWNSAMPLING: 43MB is too big. We skip points to save memory.
            // step = 10 means we only take 1 out of every 10 points.
            // This reduces 100,000 points to 10,000 (Safe for Google Maps).
            var counter = 0
            val step = 10

            reader.beginObject() // Start {
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "features") {
                    reader.beginArray() // Start [
                    while (reader.hasNext()) {
                        // We are inside a Feature
                        readFeature(reader, coordinates, counter, step)
                        counter++
                    }
                    reader.endArray() // End ]
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject() // End }
            reader.close()

            // Close the polygon loop
            if (coordinates.isNotEmpty() && coordinates.first() != coordinates.last()) {
                coordinates.add(coordinates.first())
            }

            cachedPolygon = coordinates
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext coordinates
    }

    private fun readFeature(reader: JsonReader, list: MutableList<LatLng>, index: Int, step: Int) {
        reader.beginObject()
        var lat = 0.0
        var lng = 0.0
        var isPoint = false

        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "geometry") {
                reader.beginObject()
                while (reader.hasNext()) {
                    val geoName = reader.nextName()
                    if (geoName == "type") {
                        val type = reader.nextString()
                        if (type == "Point") isPoint = true
                    } else if (geoName == "coordinates") {
                        reader.beginArray()
                        // GeoJSON is [Longitude, Latitude]
                        if (reader.hasNext()) lng = reader.nextDouble()
                        if (reader.hasNext()) lat = reader.nextDouble()
                        // Consume remaining coordinates (z-index etc) if any
                        while (reader.hasNext()) reader.skipValue()
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        // 🟢 Only add the point if it matches our step (e.g., every 10th point)
        // OR if it's the very first point (to ensure we have a start)
        if (isPoint && (index % step == 0 || index == 0)) {
            list.add(LatLng(lat, lng))
        }
    }

    // --- Keep your math helper ---
    fun findNearestPointOnPolygon(userLocation: LatLng, polygon: List<LatLng>): LatLng {
        // (Keep your existing math logic here)
        // ...
        return userLocation // Placeholder: paste your previous math code here
    }
}