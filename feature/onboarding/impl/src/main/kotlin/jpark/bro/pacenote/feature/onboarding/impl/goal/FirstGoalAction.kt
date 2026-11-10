package jpark.bro.pacenote.feature.onboarding.impl.goal

import jpark.bro.pacenote.core.model.GoalType

internal sealed interface FirstGoalAction {
    data class TypeSelected(
        val type: GoalType,
    ) : FirstGoalAction

    data object DecreaseClicked : FirstGoalAction

    data object IncreaseClicked : FirstGoalAction

    data object StartClicked : FirstGoalAction

    data object SkipClicked : FirstGoalAction

    data object BackClicked : FirstGoalAction
}
