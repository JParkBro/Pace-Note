package jpark.bro.pacenote.feature.run.impl

sealed interface RunAction {
    /** Root가 화면 시작 · 복귀 · 권한 결과 때 기기 상태를 알려준다 */
    data class DeviceChecked(
        val fineLocationGranted: Boolean,
        val coarseLocationGranted: Boolean,
        val locationEnabled: Boolean,
        val ignoringBatteryOptimizations: Boolean,
    ) : RunAction

    data object RequestPermissionClicked : RunAction

    data object OpenAppSettingsClicked : RunAction

    data object BatterySettingsClicked : RunAction

    data object BatteryLaterClicked : RunAction

    data object TurnOnLocationClicked : RunAction

    /** P1-c "시작" · "신호 없이 시작" */
    data object StartClicked : RunAction

    data object CountdownTapped : RunAction

    data object PauseClicked : RunAction

    data object ResumeClicked : RunAction

    data object LockClicked : RunAction

    data object UnlockLongPressed : RunAction

    /** P4 종료(길게 누르기) → P5 시트 */
    data object StopLongPressed : RunAction

    data object FinishSheetDismissed : RunAction

    /** P5 "계속 달리기" */
    data object ContinueClicked : RunAction

    data object SaveClicked : RunAction

    data object DiscardClicked : RunAction

    data object RecoverContinueClicked : RunAction

    data object RecoverSaveClicked : RunAction

    data object RecoverDiscardClicked : RunAction

    data object BackPressed : RunAction
}
