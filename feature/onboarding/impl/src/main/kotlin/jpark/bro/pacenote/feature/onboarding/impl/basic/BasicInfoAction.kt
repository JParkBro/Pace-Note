package jpark.bro.pacenote.feature.onboarding.impl.basic

import jpark.bro.pacenote.core.model.DistanceUnit

internal sealed interface BasicInfoAction {
    data class WeightChanged(
        val text: String,
    ) : BasicInfoAction

    data class UnitSelected(
        val unit: DistanceUnit,
    ) : BasicInfoAction

    data object NextClicked : BasicInfoAction

    data object SkipClicked : BasicInfoAction
}
