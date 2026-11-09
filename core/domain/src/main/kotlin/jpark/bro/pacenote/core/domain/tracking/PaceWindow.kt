package jpark.bro.pacenote.core.domain.tracking

/** 현재 페이스. 최근 200m 또는 최근 30초 중 먼저 채워지는 창의 평균이다 (docs/run/tracking.md 5장). */
class PaceWindow {
    private data class Sample(
        val distanceM: Double,
        val movingMs: Long,
    )

    private val samples = ArrayDeque<Sample>()

    /** 누적 거리와 누적 운동 시간을 기록한다. */
    fun add(
        distanceM: Double,
        movingMs: Long,
    ) {
        samples.addLast(Sample(distanceM, movingMs))
        while (samples.size >= 2 && isFull(samples[1], samples.last())) samples.removeFirst()
    }

    /** 창이 덜 찼거나 20′/km보다 느리면 null */
    fun currentPaceSecPerKm(): Double? {
        val newest = samples.lastOrNull() ?: return null
        val oldest = samples.first()
        val distanceM = newest.distanceM - oldest.distanceM
        val pace = (newest.movingMs - oldest.movingMs) / MS_PER_SECOND / (distanceM / METERS_PER_KM)
        return pace.takeIf { isFull(oldest, newest) && distanceM > 0 && it <= SLOWEST_PACE_SEC_PER_KM }
    }

    private fun isFull(
        from: Sample,
        to: Sample,
    ): Boolean = to.distanceM - from.distanceM >= WINDOW_DISTANCE_M || to.movingMs - from.movingMs >= WINDOW_MS

    private companion object {
        const val WINDOW_DISTANCE_M = 200.0
        const val WINDOW_MS = 30_000L
        const val SLOWEST_PACE_SEC_PER_KM = 1_200.0
        const val MS_PER_SECOND = 1_000.0
        const val METERS_PER_KM = 1_000.0
    }
}
