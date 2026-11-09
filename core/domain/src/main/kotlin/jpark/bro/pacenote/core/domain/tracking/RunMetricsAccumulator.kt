package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.model.RunSplit

/** 점 하나를 처리한 결과. [accepted]는 `run_point.accepted`, [segment]는 `run_point.segment`에 저장한다. */
data class PointOutcome(
    val decision: PointDecision,
    val accepted: Boolean,
    val segment: Int,
    val pauseState: PauseState,
)

data class RunTotals(
    val distanceM: Double,
    val movingMs: Long,
    val segmentCount: Int,
    val splits: List<RunSplit>,
)

/** 기록 하나의 거리 · 운동 시간 · 페이스 · 구간을 누적한다. 시각은 모두 단조 시계 값이다. */
class RunMetricsAccumulator(
    startElapsedMs: Long,
    private val autoPauseEnabled: Boolean,
) {
    private val detector = AutoPauseDetector()
    private val paceWindow = PaceWindow()
    private val splitCalculator = SplitCalculator()
    private val movingClock = MovingClock(startElapsedMs)

    private var state = PauseState.RUNNING
    private var distanceM = 0.0
    private var segment = 0
    private var anchor: RawLocation? = null
    private var anchorMovingMs = 0L
    private var previousElapsedMs: Long? = null
    private var lastUsableElapsedMs: Long? = null

    fun onLocation(
        point: RawLocation,
        receivedAtElapsedMs: Long = point.elapsedRealtimeMs,
    ): PointOutcome {
        val decision = PointFilter.evaluate(point, previousElapsedMs, anchor, receivedAtElapsedMs)
        if (decision != PointDecision.REJECTED_STALE) previousElapsedMs = point.elapsedRealtimeMs
        val accepted = decision.isUsable && state != PauseState.USER_PAUSED && applyUsable(point, decision)
        if (decision.isUsable) lastUsableElapsedMs = point.elapsedRealtimeMs
        return PointOutcome(decision, accepted, segment, state)
    }

    fun pause(elapsedMs: Long) {
        movingClock.stop(elapsedMs)
        state = PauseState.USER_PAUSED
        detector.reset()
    }

    /** 일시정지 중 이동한 거리는 넣지 않고 새 구간으로 이어간다. */
    fun resume(elapsedMs: Long) {
        if (state != PauseState.USER_PAUSED) return
        state = PauseState.RUNNING
        movingClock.start(elapsedMs)
        anchor = null
        lastUsableElapsedMs = null
        segment++
    }

    fun metrics(nowElapsedMs: Long): LiveMetrics {
        val moving = movingClock.at(nowElapsedMs)
        val pace =
            (moving / MS_PER_SECOND / (distanceM / METERS_PER_KM)).takeIf {
                distanceM >= MIN_AVG_PACE_DISTANCE_M
            }
        return LiveMetrics(distanceM, moving, nowElapsedMs, paceWindow.currentPaceSecPerKm(), pace)
    }

    fun finish(nowElapsedMs: Long): RunTotals {
        movingClock.stop(nowElapsedMs)
        val movingMs = movingClock.at(nowElapsedMs)
        return RunTotals(distanceM, movingMs, segment + 1, splitCalculator.finish(distanceM, movingMs))
    }

    private fun applyUsable(
        point: RawLocation,
        decision: PointDecision,
    ): Boolean {
        val signalGap = isSignalGap(point)
        if (signalGap) startSegment(point) else applyAutoPause(point)
        if (state != PauseState.RUNNING) return false
        val moving = movingClock.at(point.elapsedRealtimeMs)
        val added = !signalGap && decision == PointDecision.ACCEPTED
        if (added) addDistance(point, moving)
        paceWindow.add(distanceM, moving)
        return added || signalGap
    }

    private fun isSignalGap(point: RawLocation): Boolean =
        lastUsableElapsedMs?.let { point.elapsedRealtimeMs - it > MAX_CONNECTED_GAP_MS } ?: false

    private fun startSegment(point: RawLocation) {
        segment++
        detector.reset()
        if (state == PauseState.AUTO_PAUSED) {
            state = PauseState.RUNNING
            movingClock.start(point.elapsedRealtimeMs)
        }
        anchor = point
        anchorMovingMs = movingClock.at(point.elapsedRealtimeMs)
    }

    private fun applyAutoPause(point: RawLocation) {
        when (if (autoPauseEnabled) detector.onPoint(point) else null) {
            AutoPauseTransition.PAUSE -> {
                movingClock.stop(point.elapsedRealtimeMs)
                state = PauseState.AUTO_PAUSED
            }

            AutoPauseTransition.RESUME -> {
                state = PauseState.RUNNING
                movingClock.start(point.elapsedRealtimeMs)
            }

            null -> {
                Unit
            }
        }
    }

    private fun addDistance(
        point: RawLocation,
        moving: Long,
    ) {
        val previous = anchor
        if (previous != null) {
            val before = distanceM
            distanceM += point.distanceTo(previous)
            splitCalculator.add(before, distanceM, anchorMovingMs, moving)
        }
        anchor = point
        anchorMovingMs = moving
    }

    private companion object {
        const val MAX_CONNECTED_GAP_MS = 30_000L
        const val MIN_AVG_PACE_DISTANCE_M = 50.0
        const val MS_PER_SECOND = 1_000.0
        const val METERS_PER_KM = 1_000.0
    }
}

/** 일시정지를 뺀 운동 시간 */
private class MovingClock(
    startElapsedMs: Long,
) {
    private var accumulatedMs = 0L
    private var runningSinceMs: Long? = startElapsedMs

    fun at(elapsedMs: Long): Long = accumulatedMs + (runningSinceMs?.let { elapsedMs - it } ?: 0L)

    fun stop(elapsedMs: Long) {
        accumulatedMs = at(elapsedMs)
        runningSinceMs = null
    }

    fun start(elapsedMs: Long) {
        runningSinceMs = elapsedMs
    }
}
