package jpark.bro.pacenote.update

sealed interface UpdateAction {
    /** 새로 시작한 Activity의 onCreate (회전 · 다크 모드 변경으로 다시 만들어질 때는 보내지 않는다) */
    data object AppStarted : UpdateAction

    /** onResume: 백그라운드 다운로드가 끝났는지 다시 확인 */
    data object AppResumed : UpdateAction

    data object UpdateConfirmed : UpdateAction

    data object UpdateDismissed : UpdateAction

    data object InstallConfirmed : UpdateAction

    data object InstallDismissed : UpdateAction
}
