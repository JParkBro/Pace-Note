package jpark.bro.pacenote.core.tracking

import kotlinx.coroutines.flow.StateFlow

/** 화면이 추적에 접근하는 유일한 입구 (tracking.md 2장). 상태에 맞지 않는 호출은 무시하거나 실패를 돌려준다. */
interface RunTracker {
    val state: StateFlow<TrackingState>

    /** Idle → Preparing. 위치를 받기 시작한다. 정밀 위치 권한 · 기기 위치 켜짐은 호출 전에 확인한다. */
    suspend fun prepare()

    /** Preparing → Idle. 위치 요청을 해제한다. */
    suspend fun cancelPreparing()

    /**
     * Preparing → Recording. [recoverRunId]가 있으면 그 기록을 이어서 달린다(P8 "이어서 달리기").
     * 성공하면 기록 id
     */
    suspend fun start(recoverRunId: String? = null): Result<String>

    /** 사용자 일시정지. 자동 재개하지 않는다. */
    suspend fun pause()

    suspend fun resume()

    /** Recording → Idle. 저장했으면 기록 id, 버렸으면 null */
    suspend fun finish(save: Boolean): Result<String?>

    /** 프로세스가 죽어 남은 진행 중 기록. 지금 기록 중이면 null */
    suspend fun recoverableRun(): RecoverableRun?

    /** P8 "저장하고 마치기"(마지막 저장된 점까지) · "기록 버리기". Idle에서만 */
    suspend fun finishRecovered(
        runId: String,
        save: Boolean,
    ): Result<Unit>
}
