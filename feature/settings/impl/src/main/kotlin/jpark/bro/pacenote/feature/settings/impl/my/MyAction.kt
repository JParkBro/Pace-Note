package jpark.bro.pacenote.feature.settings.impl.my

import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType

internal sealed interface MyAction {
    data object GoalClicked : MyAction

    data object RunSettingsClicked : MyAction

    data object ProfileClicked : MyAction

    /** 테마 시트(M5) 열기 */
    data object ThemeClicked : MyAction

    data class ThemeTypeSelected(
        val type: AppThemeType,
    ) : MyAction

    data class ThemeModeSelected(
        val mode: AppThemeMode,
    ) : MyAction

    data object ThemeApplyClicked : MyAction

    /** 적용하지 않고 닫으면 원래 테마로 돌아간다 */
    data object ThemeSheetDismissed : MyAction

    data object ContactClicked : MyAction
}
