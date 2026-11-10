package jpark.bro.pacenote.feature.settings.impl.profile

import jpark.bro.pacenote.core.model.DistanceUnit

/**
 * @property weightText 체중 입력 그대로. 비우면 체중 없음(칼로리를 보여주지 않음)
 * @property weightInvalid 20 ~ 300kg 밖이거나 숫자가 아니다
 */
internal data class ProfileEditState(
    val loading: Boolean = true,
    val weightText: String = "",
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val weightInvalid: Boolean = false,
    val saving: Boolean = false,
) {
    companion object {
        const val MIN_WEIGHT_KG = 20.0
        const val MAX_WEIGHT_KG = 300.0
        const val WEIGHT_MAX_LENGTH = 5
    }
}
