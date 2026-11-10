package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RunSplit
import kotlin.math.roundToLong

/** km 구간. 1,000m 배수를 넘는 순간을 두 점 사이 선형 보간으로 찾는다 (docs/run/tracking.md 5장). */
class SplitCalculator(
    restoredSplits: List<RunSplit> = emptyList(),
) {
    private val completed = restoredSplits.toMutableList()
    private var lastBoundaryMovingMs = restoredSplits.sumOf { it.movingMs }

    /** 지금까지 완성된 1km 구간. 기록 중 `run_split`에 바로 저장해 복구 때 다시 읽는다. */
    val completedSplits: List<RunSplit> get() = completed.toList()

    /** 거리가 한 번 늘어난 구간을 넣는다. 신호 공백 뒤처럼 여러 km를 한 번에 넘어도 된다. */
    fun add(
        distanceBeforeM: Double,
        distanceAfterM: Double,
        movingBeforeMs: Long,
        movingAfterMs: Long,
    ) {
        var boundaryM = (completed.size + 1) * SPLIT_M
        while (distanceAfterM >= boundaryM) {
            val ratio = (boundaryM - distanceBeforeM) / (distanceAfterM - distanceBeforeM)
            val movingAtBoundaryMs = movingBeforeMs + ((movingAfterMs - movingBeforeMs) * ratio).roundToLong()
            completed += RunSplit(completed.size + 1, SPLIT_M, movingAtBoundaryMs - lastBoundaryMovingMs)
            lastBoundaryMovingMs = movingAtBoundaryMs
            boundaryM += SPLIT_M
        }
    }

    /** 완성된 구간과 남은 구간. 남은 거리가 [MIN_LAST_SPLIT_M] 미만이면 표에 넣지 않는다. */
    fun finish(
        totalDistanceM: Double,
        totalMovingMs: Long,
    ): List<RunSplit> {
        val remainderM = totalDistanceM - completed.size * SPLIT_M
        val last =
            RunSplit(completed.size + 1, remainderM, totalMovingMs - lastBoundaryMovingMs)
                .takeIf { remainderM >= MIN_LAST_SPLIT_M }
        return completed + listOfNotNull(last)
    }

    private companion object {
        const val SPLIT_M = 1_000.0
        const val MIN_LAST_SPLIT_M = 10.0
    }
}
