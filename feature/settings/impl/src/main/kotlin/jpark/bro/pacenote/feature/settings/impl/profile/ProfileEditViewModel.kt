package jpark.bro.pacenote.feature.settings.impl.profile

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

/** 내 정보 수정(M4): 체중(칼로리 계산용, 선택) · 거리 단위 */
internal class ProfileEditViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<ProfileEditState, ProfileEditAction, ProfileEditEvent>(ProfileEditState()) {
    init {
        viewModelScope.launch {
            val profile = settingsRepository.observeProfile().first()
            setState {
                copy(
                    loading = false,
                    weightText =
                        profile.weightKg
                            ?.let {
                                BigDecimal
                                    .valueOf(
                                        it,
                                    ).stripTrailingZeros()
                                    .toPlainString()
                            }.orEmpty(),
                    distanceUnit = profile.distanceUnit,
                )
            }
        }
    }

    override fun onAction(action: ProfileEditAction) {
        when (action) {
            is ProfileEditAction.WeightChanged -> onWeightChanged(action.text)
            is ProfileEditAction.UnitSelected -> setState { copy(distanceUnit = action.unit) }
            ProfileEditAction.SaveClicked -> save()
            ProfileEditAction.BackClicked -> sendEvent(ProfileEditEvent.Close)
        }
    }

    private fun onWeightChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }.take(ProfileEditState.WEIGHT_MAX_LENGTH)
        setState { copy(weightText = filtered, weightInvalid = false) }
    }

    private fun save() {
        val state = currentState
        if (state.loading || state.saving) return
        val weight = state.weightText.toDoubleOrNull()
        val valid =
            state.weightText.isBlank() ||
                (weight != null && weight in ProfileEditState.MIN_WEIGHT_KG..ProfileEditState.MAX_WEIGHT_KG)
        if (!valid) {
            setState { copy(weightInvalid = true) }
            return
        }
        setState { copy(saving = true) }
        viewModelScope.launch {
            settingsRepository
                .setProfile(UserProfile(weightKg = weight, distanceUnit = state.distanceUnit))
                .onSuccess { sendEvent(ProfileEditEvent.Close) }
                .onFailure { sendEvent(ProfileEditEvent.ShowError) }
            setState { copy(saving = false) }
        }
    }
}
