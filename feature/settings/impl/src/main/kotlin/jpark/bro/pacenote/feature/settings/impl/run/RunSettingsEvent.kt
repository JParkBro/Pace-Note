package jpark.bro.pacenote.feature.settings.impl.run

internal sealed interface RunSettingsEvent {
    data object NavigateBack : RunSettingsEvent

    data object ShowBatteryGuideReset : RunSettingsEvent

    data object ShowError : RunSettingsEvent
}
