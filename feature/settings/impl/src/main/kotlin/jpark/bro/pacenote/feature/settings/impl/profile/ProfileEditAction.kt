package jpark.bro.pacenote.feature.settings.impl.profile

import jpark.bro.pacenote.core.model.DistanceUnit

internal sealed interface ProfileEditAction {
    data class WeightChanged(
        val text: String,
    ) : ProfileEditAction

    data class UnitSelected(
        val unit: DistanceUnit,
    ) : ProfileEditAction

    data object SaveClicked : ProfileEditAction

    data object BackClicked : ProfileEditAction
}
