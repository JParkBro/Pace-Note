package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation

private const val METERS_PER_DEGREE_LAT = 111_194.92664455873

/** 원점에서 북쪽으로 [northM]미터 떨어진 점 */
internal fun pointAt(
    northM: Double,
    elapsedMs: Long,
    accuracyM: Float = 5f,
): RawLocation =
    RawLocation(
        lat = northM / METERS_PER_DEGREE_LAT,
        lng = 0.0,
        altitude = null,
        accuracyM = accuracyM,
        speedMps = null,
        recordedAt = elapsedMs,
        elapsedRealtimeMs = elapsedMs,
    )
