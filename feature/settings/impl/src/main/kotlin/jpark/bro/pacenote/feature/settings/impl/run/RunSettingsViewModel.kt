package jpark.bro.pacenote.feature.settings.impl.run

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.VoiceItem
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 러닝 설정(M3, product-spec 2-5 · 5-2 · 5-3). 바꾸는 즉시 저장한다.
 * 추적 엔진이 러닝을 시작할 때 이 값을 읽으므로 다음 러닝부터 적용된다.
 */
internal class RunSettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : MviViewModel<RunSettingsState, RunSettingsAction, RunSettingsEvent>(RunSettingsState()) {
    init {
        settingsRepository
            .observeRunSettings()
            .onEach { settings -> setState { copy(loading = false, settings = settings) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RunSettingsAction) {
        when (action) {
            is RunSettingsAction.VoiceToggled -> update { copy(voiceEnabled = action.enabled) }
            is RunSettingsAction.IntervalSelected -> update { copy(voiceInterval = action.interval) }
            is RunSettingsAction.ItemToggled -> toggleItem(action.item)
            is RunSettingsAction.AutoPauseToggled -> update { copy(autoPauseEnabled = action.enabled) }
            is RunSettingsAction.CountdownSelected -> update { copy(countdownSeconds = action.seconds) }
            is RunSettingsAction.KeepScreenOnToggled -> update { copy(keepScreenOn = action.enabled) }
            RunSettingsAction.BatteryGuideResetClicked -> resetBatteryGuide()
            RunSettingsAction.BackClicked -> sendEvent(RunSettingsEvent.NavigateBack)
        }
    }

    /** 안내 항목은 하나 이상 남긴다 (모두 끄려면 음성 안내를 끈다) */
    private fun toggleItem(item: VoiceItem) {
        val items = currentState.settings.voiceItems
        if (item in items && items.size == 1) return
        update { copy(voiceItems = if (item in items) items - item else items + item) }
    }

    private fun update(change: RunSettings.() -> RunSettings) {
        if (currentState.loading) return
        val next = currentState.settings.change()
        setState { copy(settings = next) }
        viewModelScope.launch {
            settingsRepository.setRunSettings(next).onFailure { sendEvent(RunSettingsEvent.ShowError) }
        }
    }

    private fun resetBatteryGuide() {
        viewModelScope.launch {
            settingsRepository
                .setBatteryGuideShown(false)
                .onSuccess { sendEvent(RunSettingsEvent.ShowBatteryGuideReset) }
                .onFailure { sendEvent(RunSettingsEvent.ShowError) }
        }
    }
}
