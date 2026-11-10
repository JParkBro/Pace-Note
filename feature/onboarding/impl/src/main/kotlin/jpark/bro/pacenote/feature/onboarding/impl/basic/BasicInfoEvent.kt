package jpark.bro.pacenote.feature.onboarding.impl.basic

internal sealed interface BasicInfoEvent {
    /** 첫 목표(O3)로 */
    data object OpenGoal : BasicInfoEvent

    data object ShowError : BasicInfoEvent
}
