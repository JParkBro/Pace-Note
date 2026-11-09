package jpark.bro.pacenote.core.tracking

import jpark.bro.pacenote.core.domain.tracking.RunMetricsAccumulator
import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.model.RunStatus

/** 기록 중인 한 번의 러닝: 계산(`RunMetricsAccumulator`)과 저장(`RunRecorder`)을 묶는다. */
internal class RecordingSession(
    val runId: String,
    private val accumulator: RunMetricsAccumulator,
    private val recorder: RunRecorder,
    initialRoute: List<RoutePoint>,
) {
    private var route = initialRoute

    val pauseState: PauseState get() = accumulator.pauseState

    suspend fun onPoint(
        point: RawLocation,
        nowElapsedMs: Long,
    ) {
        val outcome = accumulator.onLocation(point, nowElapsedMs)
        recorder.add(point, outcome)
        if (outcome.accepted) route = route + RoutePoint(point.lat, point.lng, outcome.segment)
        flushIfDue(nowElapsedMs)
    }

    suspend fun flushIfDue(nowElapsedMs: Long) {
        if (recorder.isFlushDue(nowElapsedMs)) flush(nowElapsedMs)
    }

    suspend fun pause(nowElapsedMs: Long) {
        accumulator.pause(nowElapsedMs)
        flush(nowElapsedMs)
        recorder.setStatus(RunStatus.PAUSED, pauseIncrement = 1)
    }

    suspend fun resume(nowElapsedMs: Long) {
        accumulator.resume(nowElapsedMs)
        flush(nowElapsedMs)
        recorder.setStatus(RunStatus.ACTIVE, pauseIncrement = 0)
    }

    suspend fun finish(
        save: Boolean,
        weightKg: Double?,
        nowElapsedMs: Long,
    ) {
        flush(nowElapsedMs)
        if (save) {
            recorder.finish(accumulator.finish(nowElapsedMs), weightKg)
        } else {
            // 점은 정리 작업이 하루 뒤 지운다 (tracking.md 6-1)
            recorder.setStatus(RunStatus.DISCARDED, pauseIncrement = 0)
        }
    }

    fun snapshot(
        nowElapsedMs: Long,
        gps: GpsStatus,
    ) = TrackingState.Recording(runId, accumulator.metrics(nowElapsedMs), route, accumulator.pauseState, gps)

    private suspend fun flush(nowElapsedMs: Long) {
        val metrics = accumulator.metrics(nowElapsedMs)
        recorder.flush(metrics.distanceM, metrics.movingMsAtEmit, accumulator.completedSplits, nowElapsedMs)
    }
}
