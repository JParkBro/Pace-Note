package jpark.bro.pacenote.feature.home.impl

internal sealed interface HomeAction {
    /** 화면이 다시 보일 때마다 Root가 보낸다. 그 사이 주가 바뀌었으면 이번 주를 다시 잡는다 */
    data object ScreenStarted : HomeAction

    data object StartRunClicked : HomeAction

    /** 목표가 없을 때 "목표 정하기" */
    data object SetGoalClicked : HomeAction

    data class RunClicked(
        val runId: String,
    ) : HomeAction
}
