package jpark.bro.pacenote.feature.settings.impl.my

internal sealed interface MyEvent {
    data object OpenGoal : MyEvent

    data object OpenProfile : MyEvent
}
