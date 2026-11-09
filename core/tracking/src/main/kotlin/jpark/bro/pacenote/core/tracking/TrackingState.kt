package jpark.bro.pacenote.core.tracking

import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RoutePoint

/**
 * 추적 상태 (tracking.md 3장). 화면은 이것을 옮기기만 하고 시간 · 거리를 계산하지 않는다.
 * 전이: Idle → Preparing → Recording(Running ⇄ Paused) → Finishing → Idle
 */
sealed interface TrackingState {
    data object Idle : TrackingState

    /** 준비(P1-c): GPS 신호 대기 */
    data class Preparing(
        val gps: GpsStatus,
    ) : TrackingState

    /** @property route 거리에 들어간 점. 화면용 단순화는 그리는 쪽(`RouteCanvas`)이 한다 */
    data class Recording(
        val runId: String,
        val metrics: LiveMetrics,
        val route: List<RoutePoint>,
        val pause: PauseState,
        val gps: GpsStatus,
    ) : TrackingState

    data class Finishing(
        val runId: String,
    ) : TrackingState
}

/** 프로세스가 죽어 남은 진행 중 기록 (P8). [startedAt]으로 "어제 시작한 기록이에요"를 판단한다. */
data class RecoverableRun(
    val runId: String,
    val startedAt: Long,
    val distanceM: Double,
    val movingMs: Long,
)
