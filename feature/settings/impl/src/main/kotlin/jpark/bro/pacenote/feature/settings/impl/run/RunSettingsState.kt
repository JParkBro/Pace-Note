package jpark.bro.pacenote.feature.settings.impl.run

import jpark.bro.pacenote.core.model.RunSettings

internal data class RunSettingsState(
    val loading: Boolean = true,
    val settings: RunSettings = RunSettings(),
)
