package jpark.bro.pacenote.feature.onboarding.impl.basic

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.launch

/** 기본 정보(O2): 체중 · 거리 단위. 건너뛰면 저장하지 않고 다음으로 (product-spec 4-2) */
internal class BasicInfoViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<BasicInfoState, BasicInfoAction, BasicInfoEvent>(BasicInfoState()) {
    override fun onAction(action: BasicInfoAction) {
        when (action) {
            is BasicInfoAction.WeightChanged -> onWeightChanged(action.text)
            is BasicInfoAction.UnitSelected -> setState { copy(distanceUnit = action.unit) }
            BasicInfoAction.NextClicked -> save()
            BasicInfoAction.SkipClicked -> sendEvent(BasicInfoEvent.OpenGoal)
        }
    }

    private fun onWeightChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }.take(BasicInfoState.WEIGHT_MAX_LENGTH)
        setState { copy(weightText = filtered, weightInvalid = false) }
    }

    private fun save() {
        val state = currentState
        if (state.saving) return
        val weight = state.weightText.toDoubleOrNull()
        val valid =
            state.weightText.isBlank() ||
                (weight != null && weight in BasicInfoState.MIN_WEIGHT_KG..BasicInfoState.MAX_WEIGHT_KG)
        if (!valid) {
            setState { copy(weightInvalid = true) }
            return
        }
        setState { copy(saving = true) }
        viewModelScope.launch {
            settingsRepository
                .setProfile(UserProfile(weightKg = weight, distanceUnit = state.distanceUnit))
                .onSuccess { sendEvent(BasicInfoEvent.OpenGoal) }
                .onFailure { sendEvent(BasicInfoEvent.ShowError) }
            setState { copy(saving = false) }
        }
    }
}
