package jpark.bro.pacenote.core.model

/**
 * 위치 제공자에서 받은 점 하나.
 *
 * @property recordedAt 벽시계 시각 (epoch millis). 저장 · 표시용
 * @property elapsedRealtimeMs 단조 시계 시각. 필터와 시간 계산용
 */
data class RawLocation(
    val lat: Double,
    val lng: Double,
    val altitude: Double?,
    val accuracyM: Float,
    val speedMps: Float?,
    val recordedAt: Long,
    val elapsedRealtimeMs: Long,
)
