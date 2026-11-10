package jpark.bro.pacenote.feature.settings.impl.run

import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem

internal sealed interface RunSettingsAction {
    data class VoiceToggled(
        val enabled: Boolean,
    ) : RunSettingsAction

    data class IntervalSelected(
        val interval: VoiceInterval,
    ) : RunSettingsAction

    data class ItemToggled(
        val item: VoiceItem,
    ) : RunSettingsAction

    data class AutoPauseToggled(
        val enabled: Boolean,
    ) : RunSettingsAction

    data class CountdownSelected(
        val seconds: Int,
    ) : RunSettingsAction

    data class KeepScreenOnToggled(
        val enabled: Boolean,
    ) : RunSettingsAction

    /** P1-b 배터리 최적화 안내를 다음 러닝에 다시 보여준다 */
    data object BatteryGuideResetClicked : RunSettingsAction

    data object BackClicked : RunSettingsAction
}
