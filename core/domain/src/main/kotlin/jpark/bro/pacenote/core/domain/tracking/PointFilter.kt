package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation
import kotlin.math.max

enum class PointDecision {
    ACCEPTED,
    REJECTED_ACCURACY,
    REJECTED_STALE,
    REJECTED_SPEED,
    REJECTED_JITTER,
    ;

    /** 거리에는 넣지 않더라도 정지 판단과 신호 공백 판단에 쓸 수 있는 점인가 */
    val isUsable: Boolean get() = this == ACCEPTED || this == REJECTED_JITTER
}

/** 점 필터. 값의 근거는 docs/run/tracking.md 4-2. */
object PointFilter {
    private const val MAX_ACCURACY_M = 25f
    private const val MAX_POINT_AGE_MS = 10_000L
    private const val MAX_SPEED_MPS = 12.0
    private const val MIN_MOVE_M = 3.0
    private const val MIN_MOVE_ACCURACY_RATIO = 0.5

    /**
     * @param previousElapsedMs 직전에 받은 점의 시각
     * @param lastAccepted 마지막 채택 점. 튄 점 뒤의 정상 점까지 버리지 않도록 직전 점이 아니라 이 점과 비교한다
     * @param receivedAtElapsedMs 이 점을 받은 시각
     */
    fun evaluate(
        point: RawLocation,
        previousElapsedMs: Long?,
        lastAccepted: RawLocation?,
        receivedAtElapsedMs: Long,
    ): PointDecision =
        when {
            point.accuracyM > MAX_ACCURACY_M -> PointDecision.REJECTED_ACCURACY
            isStale(point, previousElapsedMs, receivedAtElapsedMs) -> PointDecision.REJECTED_STALE
            lastAccepted == null -> PointDecision.ACCEPTED
            point.speedFrom(lastAccepted) > MAX_SPEED_MPS -> PointDecision.REJECTED_SPEED
            point.distanceTo(lastAccepted) < minMoveM(point) -> PointDecision.REJECTED_JITTER
            else -> PointDecision.ACCEPTED
        }

    private fun isStale(
        point: RawLocation,
        previousElapsedMs: Long?,
        receivedAtElapsedMs: Long,
    ): Boolean =
        (previousElapsedMs != null && point.elapsedRealtimeMs <= previousElapsedMs) ||
            receivedAtElapsedMs - point.elapsedRealtimeMs > MAX_POINT_AGE_MS

    private fun minMoveM(point: RawLocation): Double = max(MIN_MOVE_M, point.accuracyM * MIN_MOVE_ACCURACY_RATIO)
}
