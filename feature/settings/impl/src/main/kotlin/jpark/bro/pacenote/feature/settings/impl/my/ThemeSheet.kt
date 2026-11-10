package jpark.bro.pacenote.feature.settings.impl.my

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunBottomSheet
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSegment
import jpark.bro.pacenote.core.designsystem.component.RunSegmentedControl
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.feature.settings.impl.R

private val SWATCH_SIZE = 28.dp
private val SELECTED_BORDER = 2.dp

/** 테마 시트(M5): 4 테마 + 시스템 / 라이트 / 다크. 고르는 즉시 앱 전체가 미리보기로 바뀐다. */
@Composable
internal fun ThemeSheet(
    selected: ThemeSetting,
    onAction: (MyAction) -> Unit,
) {
    RunBottomSheet(onDismissRequest = { onAction(MyAction.ThemeSheetDismissed) }) {
        Column(
            modifier = Modifier.fillMaxWidth().testTag("settings_theme_sheet"),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
            )
            AppThemeType.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.gutterCompact)) {
                    row.forEach { type ->
                        ThemeCard(
                            type = type,
                            selected = type == selected.type,
                            onClick = { onAction(MyAction.ThemeTypeSelected(type)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.settings_theme_mode),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            RunSegmentedControl(
                segments =
                    listOf(
                        RunSegment(stringResource(R.string.settings_theme_mode_system)),
                        RunSegment(stringResource(R.string.settings_theme_mode_light)),
                        RunSegment(stringResource(R.string.settings_theme_mode_dark)),
                    ),
                selectedIndex = MODES.indexOf(selected.mode),
                onSelect = { onAction(MyAction.ThemeModeSelected(MODES[it])) },
                modifier = Modifier.testTag("settings_theme_mode"),
            )
            RunPrimaryButton(
                text = stringResource(R.string.settings_theme_apply),
                onClick = { onAction(MyAction.ThemeApplyClicked) },
                modifier = Modifier.fillMaxWidth().testTag("settings_theme_apply"),
            )
        }
    }
}

/** 세그먼트 순서: 시스템 · 라이트 · 다크 */
private val MODES = listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK)

@Composable
private fun ThemeCard(
    type: AppThemeType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .clip(AppShapes.lg)
                .background(colors.surface)
                .border(
                    if (selected) SELECTED_BORDER else 1.dp,
                    if (selected) colors.primary else colors.border,
                    AppShapes.lg,
                ).clickable(role = Role.RadioButton, onClick = onClick)
                .semantics { this.selected = selected }
                .padding(AppSpacing.md)
                .testTag("settings_theme_${type.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Swatch(type)
        Text(
            text = stringResource(themeName(type)),
            style = AppThemeTokens.typography.bodyMd,
            color = colors.textPrimary,
        )
    }
}

/** 그 테마의 primary 색 동그라미. 그 테마로 감싸 토큰만 읽는다 */
@Composable
private fun Swatch(type: AppThemeType) {
    AppTheme(type = type, mode = AppThemeMode.DARK) {
        Box(
            modifier = Modifier.size(SWATCH_SIZE).clip(CircleShape).background(AppThemeTokens.colors.primary),
            contentAlignment = Alignment.Center,
        ) {}
    }
}

private fun themeName(type: AppThemeType): Int =
    when (type) {
        AppThemeType.VOLT -> R.string.settings_theme_volt
        AppThemeType.SUNRISE -> R.string.settings_theme_sunrise
        AppThemeType.RIVER -> R.string.settings_theme_river
        AppThemeType.INK -> R.string.settings_theme_ink
    }
