package jpark.bro.pacenote.feature.run.impl

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.core.tracking.TrackingState

/** 러닝 화면 하나 안의 단계 (module-map 4-2) */
enum class RunStep {
    /** 권한 · 기기 상태 확인 중 */
    CHECKING,

    /** P8 복구 다이얼로그 */
    RECOVER,

    /** P1-a 권한 안내 */
    PERMISSION,

    /** P1-b 배터리 최적화 안내 */
    BATTERY,

    /** P1-c GPS 대기 */
    GPS,

    /** P2 카운트다운 */
    COUNTDOWN,

    /** P3 · P3-L · P4 (+ P5 시트) */
    RECORDING,

    /** 저장 · 버리는 중 */
    FINISHING,
}

/**
 * 러닝 화면 상태. 기록 중 수치는 `RunTracker.state`를 그대로 옮긴 [recording]이고, 화면은 시계만 보간한다.
 *
 * @property coarseOnly 대략 위치만 허용한 상태 (P1-a 재요청 안내)
 * @property isOldRecovery 진행 중 기록이 24시간 넘게 방치됨 → "어제 시작한 기록이에요", 기본 버튼은 저장
 */
data class RunState(
    val step: RunStep = RunStep.CHECKING,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val coarseOnly: Boolean = false,
    val gps: GpsStatus = GpsStatus.LOST,
    val locationEnabled: Boolean = true,
    val countdownRemaining: Int = 0,
    val recording: TrackingState.Recording? = null,
    val locked: Boolean = false,
    val showFinishSheet: Boolean = false,
    val keepScreenOn: Boolean = false,
    val recoverable: RecoverableRun? = null,
    val isOldRecovery: Boolean = false,
    val busy: Boolean = false,
)
