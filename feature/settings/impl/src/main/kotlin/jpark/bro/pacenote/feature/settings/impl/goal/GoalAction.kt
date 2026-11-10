package jpark.bro.pacenote.feature.settings.impl.goal

import jpark.bro.pacenote.core.model.GoalType

internal sealed interface GoalAction {
    data class TypeSelected(
        val type: GoalType,
    ) : GoalAction

    data object DecreaseClicked : GoalAction

    data object IncreaseClicked : GoalAction

    data object SaveClicked : GoalAction

    data object BackClicked : GoalAction
}
