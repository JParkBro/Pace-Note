package jpark.bro.pacenote.feature.settings.impl.my

internal sealed interface MyAction {
    data object GoalClicked : MyAction

    data object ProfileClicked : MyAction
}
