package jpark.bro.pacenote.feature.home.impl

internal sealed interface HomeAction {
    data object StartRunClicked : HomeAction

    /** 목표가 없을 때 "목표 정하기" */
    data object SetGoalClicked : HomeAction

    data class RunClicked(
        val runId: String,
    ) : HomeAction
}
