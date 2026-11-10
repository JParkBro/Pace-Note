package jpark.bro.pacenote.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Fused Location Provider 구현. 권한 확인은 호출하는 쪽(P1)이 한다. */
class FusedLocationSource(
    private val context: Context,
) : LocationSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // 인터페이스 계약: 호출 전 권한 확인, 없으면 SecurityException으로 끝남
    override fun locations(): Flow<RawLocation> =
        callbackFlow {
            val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.locations.forEach { trySend(it.toRawLocation()) }
                    }
                }
            client
                .requestLocationUpdates(REQUEST, callback, Looper.getMainLooper())
                .addOnFailureListener { close(it) }
            awaitClose { client.removeLocationUpdates(callback) }
        }

    @SuppressLint("MissingPermission")
    override suspend fun lastKnown(): RawLocation? =
        suspendRunCatching { client.lastLocation.await()?.toRawLocation() }.getOrNull()

    override fun isLocationEnabled(): Boolean {
        val manager = context.getSystemService(LocationManager::class.java) ?: return false
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    private companion object {
        const val INTERVAL_MS = 1_000L

        val REQUEST: LocationRequest =
            LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
                .setMinUpdateIntervalMillis(INTERVAL_MS)
                .setMinUpdateDistanceMeters(0f)
                .setWaitForAccurateLocation(false)
                .build()
    }
}

private const val NANOS_PER_MILLI = 1_000_000L

internal fun Location.toRawLocation(): RawLocation =
    RawLocation(
        lat = latitude,
        lng = longitude,
        altitude = if (hasAltitude()) altitude else null,
        accuracyM = accuracy,
        speedMps = if (hasSpeed()) speed else null,
        recordedAt = time,
        elapsedRealtimeMs = elapsedRealtimeNanos / NANOS_PER_MILLI,
    )
