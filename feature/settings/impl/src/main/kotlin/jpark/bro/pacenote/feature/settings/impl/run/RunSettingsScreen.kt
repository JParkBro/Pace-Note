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

/** 러닝 설정(M3)의 Root */
@Composable
internal fun RunSettingsRoot(
    onBack: () -> Unit,
    viewModel: RunSettingsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resetMessage = stringResource(R.string.settings_run_battery_guide_reset)
    val errorMessage = stringResource(R.string.settings_error)
    BackHandler { viewModel.onAction(RunSettingsAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            RunSettingsEvent.NavigateBack -> onBack()
            RunSettingsEvent.ShowBatteryGuideReset -> snackbarHostState.showSnackbar(resetMessage)
            RunSettingsEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    RunSettingsScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun RunSettingsScreen(
    state: RunSettingsState,
    snackbarHostState: SnackbarHostState,
    onAction: (RunSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.settings_run_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.settings_back),
                        onClick = { onAction(RunSettingsAction.BackClicked) },
                    )
                },
            )
        },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            VoiceSection(state.settings, onAction)
            RecordingSection(state.settings, onAction)
            Text(
                text = stringResource(R.string.settings_run_applies_next),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
            )
        }
    }
}

@Composable
private fun VoiceSection(
    settings: RunSettings,
    onAction: (RunSettingsAction) -> Unit,
) {
    RunSection(title = stringResource(R.string.settings_run_section_voice)) {
        SwitchRow(
            title = stringResource(R.string.settings_run_voice),
            checked = settings.voiceEnabled,
            tag = "settings_run_voice",
        ) { onAction(RunSettingsAction.VoiceToggled(it)) }
        if (settings.voiceEnabled) {
            ChipGroup(stringResource(R.string.settings_run_voice_interval)) {
                VoiceInterval.entries.forEach { interval ->
                    RunFilterChip(
                        text = stringResource(intervalLabel(interval)),
                        selected = settings.voiceInterval == interval,
                        onClick = { onAction(RunSettingsAction.IntervalSelected(interval)) },
                        modifier = Modifier.testTag("settings_run_interval_${interval.name.lowercase()}"),
                    )
                }
            }
            ChipGroup(stringResource(R.string.settings_run_voice_items)) {
                VoiceItem.entries.forEach { item ->
                    RunFilterChip(
                        text = stringResource(itemLabel(item)),
                        selected = item in settings.voiceItems,
                        onClick = { onAction(RunSettingsAction.ItemToggled(item)) },
                        role = Role.Checkbox,
                        modifier = Modifier.testTag("settings_run_item_${item.name.lowercase()}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingSection(
    settings: RunSettings,
    onAction: (RunSettingsAction) -> Unit,
) {
    RunSection(title = stringResource(R.string.settings_run_section_recording)) {
        SwitchRow(
            title = stringResource(R.string.settings_run_auto_pause),
            subtitle = stringResource(R.string.settings_run_auto_pause_desc),
            checked = settings.autoPauseEnabled,
            tag = "settings_run_auto_pause",
        ) { onAction(RunSettingsAction.AutoPauseToggled(it)) }
        ChipGroup(stringResource(R.string.settings_run_countdown)) {
            RunSettings.COUNTDOWN_OPTIONS.forEach { seconds ->
                RunFilterChip(
                    text =
                        if (seconds == 0) {
                            stringResource(R.string.settings_run_countdown_off)
                        } else {
                            stringResource(R.string.settings_run_countdown_seconds, seconds)
                        },
                    selected = settings.countdownSeconds == seconds,
                    onClick = { onAction(RunSettingsAction.CountdownSelected(seconds)) },
                    modifier = Modifier.testTag("settings_run_countdown_$seconds"),
                )
            }
        }
        SwitchRow(
            title = stringResource(R.string.settings_run_keep_screen_on),
            subtitle = stringResource(R.string.settings_run_keep_screen_on_desc),
            checked = settings.keepScreenOn,
            tag = "settings_run_keep_screen_on",
        ) { onAction(RunSettingsAction.KeepScreenOnToggled(it)) }
        RunListItem(
            title = stringResource(R.string.settings_run_battery_guide),
            onClick = { onAction(RunSettingsAction.BatteryGuideResetClicked) },
            showChevron = false,
            modifier = Modifier.testTag("settings_run_battery_guide"),
        )
    }
}

@Composable
private fun Preview(
    state: RunSettingsState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { RunSettingsScreen(state, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun RunSettingsPreview() = Preview(RunSettingsState(loading = false))

@ScreenPreviews
@Composable
private fun RunSettingsVoiceOffPreview() =
    Preview(
        RunSettingsState(loading = false, settings = RunSettings(voiceEnabled = false)),
        AppThemeType.INK,
        AppThemeMode.DARK,
    )
