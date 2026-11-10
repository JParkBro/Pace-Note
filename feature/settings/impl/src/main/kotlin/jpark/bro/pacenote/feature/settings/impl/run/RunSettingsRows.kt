package jpark.bro.pacenote.feature.settings.impl.run

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunFilterChip
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunListItem
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunSwitch
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.settings.impl.R
import jpark.bro.pacenote.core.designsystem.R as DesignR

@Composable
internal fun SwitchRow(
    title: String,
    checked: Boolean,
    tag: String,
    subtitle: String? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    RunListItem(
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        showChevron = false,
        trailing = { RunSwitch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.testTag(tag),
    )
}

@Composable
internal fun ChipGroup(
    title: String,
    chips: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(text = title, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textPrimary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) { chips() }
    }
}

internal fun intervalLabel(interval: VoiceInterval): Int =
    when (interval) {
        VoiceInterval.KM_0_5 -> R.string.settings_run_interval_0_5km
        VoiceInterval.KM_1 -> R.string.settings_run_interval_1km
        VoiceInterval.KM_2 -> R.string.settings_run_interval_2km
        VoiceInterval.MINUTES_5 -> R.string.settings_run_interval_5min
        VoiceInterval.MINUTES_10 -> R.string.settings_run_interval_10min
    }

internal fun itemLabel(item: VoiceItem): Int =
    when (item) {
        VoiceItem.DISTANCE -> R.string.settings_run_item_distance
        VoiceItem.TIME -> R.string.settings_run_item_time
        VoiceItem.AVERAGE_PACE -> R.string.settings_run_item_average_pace
        VoiceItem.SPLIT_PACE -> R.string.settings_run_item_split_pace
    }
