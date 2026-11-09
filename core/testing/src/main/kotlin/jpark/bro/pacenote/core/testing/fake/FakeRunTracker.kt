package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 화면 테스트용 추적기. 호출을 [calls]에 남기고 상태를 단순하게 바꾼다.
 * 기록 중 수치를 바꾸려면 [state]에 직접 [recording]을 넣는다.
 */
class FakeRunTracker : RunTracker {
    override val state = MutableStateFlow<TrackingState>(TrackingState.Idle)
    val calls = mutableListOf<String>()
    var recoverable: RecoverableRun? = null
    var startResult: Result<String> = Result.success(RUN_ID)
    var finishResult: Result<Unit> = Result.success(Unit)

    override suspend fun prepare() {
        calls += "prepare"
        if (state.value == TrackingState.Idle) state.value = TrackingState.Preparing(GpsStatus.LOST)
    }

    override suspend fun cancelPreparing() {
        calls += "cancelPreparing"
        state.value = TrackingState.Idle
    }

    override suspend fun start(recoverRunId: String?): Result<String> {
        calls += "start:${recoverRunId ?: "new"}"
        return startResult.onSuccess { state.value = recording(it) }
    }

    override suspend fun pause() {
        calls += "pause"
        (state.value as? TrackingState.Recording)?.let { state.value = it.copy(pause = PauseState.USER_PAUSED) }
    }

    override suspend fun resume() {
        calls += "resume"
        (state.value as? TrackingState.Recording)?.let { state.value = it.copy(pause = PauseState.RUNNING) }
    }

    override suspend fun finish(save: Boolean): Result<String?> {
        calls += "finish:$save"
        val runId = (state.value as? TrackingState.Recording)?.runId
        return finishResult
            .onSuccess { state.value = TrackingState.Idle }
            .map { runId.takeIf { save } }
    }

    override suspend fun recoverableRun(): RecoverableRun? = recoverable

    override suspend fun finishRecovered(
        runId: String,
        save: Boolean,
    ): Result<Unit> {
        calls += "finishRecovered:$runId:$save"
        return finishResult.onSuccess { recoverable = null }
    }

    companion object {
        const val RUN_ID = "run-1"
        private const val SAMPLE_DISTANCE_M = 3_420.0
        private const val SAMPLE_MOVING_MS = 1_231_000L
        private const val SAMPLE_CURRENT_PACE = 355.0
        private const val SAMPLE_AVERAGE_PACE = 360.0

        /** 샘플 시나리오의 진행 중 기록: 3.42km, 20:31, 현재 페이스 5′55″ */
        fun recording(
            runId: String = RUN_ID,
            distanceM: Double = SAMPLE_DISTANCE_M,
            pause: PauseState = PauseState.RUNNING,
        ) = TrackingState.Recording(
            runId = runId,
            metrics = LiveMetrics(distanceM, SAMPLE_MOVING_MS, 0L, SAMPLE_CURRENT_PACE, SAMPLE_AVERAGE_PACE),
            route = emptyList(),
            pause = pause,
            gps = GpsStatus.GOOD,
        )
    }
}
