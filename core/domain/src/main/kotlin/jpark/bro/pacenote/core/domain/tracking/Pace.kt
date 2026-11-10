package jpark.bro.pacenote.core.domain.tracking

/** 운동 시간 ÷ 거리로 구하는 페이스 (docs/run/tracking.md). 평균 · 구간 페이스가 모두 이것을 쓴다. */
object Pace {
    /** 이보다 짧은 거리의 페이스는 값이 크게 튀어 보여주지 않는다 */
    const val MIN_DISTANCE_M = 50.0

    private const val MS_PER_SECOND = 1_000.0
    private const val METERS_PER_KM = 1_000.0

    /**
     * km당 초. 거리가 [minDistanceM] 미만이거나 0 이하면 null.
     * 마지막 짧은 구간(10m 이상, tracking.md 5장)은 [minDistanceM]을 낮춰 환산 페이스를 구한다.
     */
    fun secPerKm(
        movingMs: Long,
        distanceM: Double,
        minDistanceM: Double = MIN_DISTANCE_M,
    ): Double? =
        (movingMs / MS_PER_SECOND / (distanceM / METERS_PER_KM)).takeIf { distanceM > 0 && distanceM >= minDistanceM }
}
