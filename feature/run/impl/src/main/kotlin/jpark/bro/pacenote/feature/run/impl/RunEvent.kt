package jpark.bro.pacenote.feature.run.impl

sealed interface RunEvent {
    data object RequestLocationPermission : RunEvent

    data object OpenAppSettings : RunEvent

    data object OpenBatterySettings : RunEvent

    /** 기기 위치 켜기 요청 (시스템 설정 대화상자) */
    data object RequestLocationSettings : RunEvent

    /** 저장했다 → 결과(P6) */
    data class OpenResult(
        val runId: String,
    ) : RunEvent

    /** 러닝 흐름을 닫는다 (버림 · 준비 취소) */
    data object Close : RunEvent

    /** 기록 중 뒤로 가기: 앱을 백그라운드로, 추적은 계속 */
    data object MoveToBackground : RunEvent

    data object ShowError : RunEvent
}
