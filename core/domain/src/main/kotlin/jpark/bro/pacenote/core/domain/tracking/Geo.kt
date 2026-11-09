package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0
private const val MS_PER_SECOND = 1_000.0

internal fun haversineM(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(a))
}

internal fun RawLocation.distanceTo(other: RawLocation): Double = haversineM(lat, lng, other.lat, other.lng)

internal fun RawLocation.speedFrom(previous: RawLocation): Double =
    distanceTo(previous) / ((elapsedRealtimeMs - previous.elapsedRealtimeMs) / MS_PER_SECOND)
