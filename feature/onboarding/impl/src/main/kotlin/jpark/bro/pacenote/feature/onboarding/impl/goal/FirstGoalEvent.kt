package jpark.bro.pacenote.feature.onboarding.impl.goal

internal sealed interface FirstGoalEvent {
    /** 온보딩 끝 → app이 홈으로 */
    data object Done : FirstGoalEvent

    data object NavigateBack : FirstGoalEvent

    data object ShowError : FirstGoalEvent
}
