package jpark.bro.pacenote.feature.settings.impl.goal

internal sealed interface GoalEvent {
    /** 저장했거나 그냥 닫는다 */
    data object Close : GoalEvent

    data object ShowError : GoalEvent
}
