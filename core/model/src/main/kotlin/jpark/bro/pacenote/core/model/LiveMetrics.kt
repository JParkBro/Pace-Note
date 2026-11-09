package jpark.bro.pacenote.core.model

/**
 * 기록 중 수치. 화면은 [movingMsAtEmit]에 [emittedAtElapsedMs] 이후 지난 시간을 더해 시계를 보간한다.
 *
 * @property currentPaceSecPerKm 데이터가 부족하거나 걷기보다 느리면 null
 * @property avgPaceSecPerKm 거리가 짧으면 null
 */
data class LiveMetrics(
    val distanceM: Double,
    val movingMsAtEmit: Long,
    val emittedAtElapsedMs: Long,
    val currentPaceSecPerKm: Double?,
    val avgPaceSecPerKm: Double?,
)
