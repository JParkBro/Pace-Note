package jpark.bro.pacenote.feature.run.impl.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunGpsIndicator
import jpark.bro.pacenote.core.designsystem.component.RunInfoBox
import jpark.bro.pacenote.core.designsystem.component.RunInfoBoxTone
import jpark.bro.pacenote.core.designsystem.component.RunOutlineButton
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.feature.run.impl.R
import jpark.bro.pacenote.feature.run.impl.RunAction
import jpark.bro.pacenote.feature.run.impl.RunState

/** 확인 중 · 저장 중 */
@Composable
internal fun RunLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppThemeTokens.colors.primary)
    }
}

/** P1-a 권한 안내 */
@Composable
internal fun RunPermissionContent(
    state: RunState,
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrepareLayout(
        title = stringResource(R.string.run_permission_title),
        body = stringResource(R.string.run_permission_body),
        modifier = modifier,
        info = if (state.coarseOnly) stringResource(R.string.run_permission_coarse_only) else null,
    ) {
        RunPrimaryButton(
            text = stringResource(R.string.run_permission_allow),
            onClick = { onAction(RunAction.RequestPermissionClicked) },
            modifier = Modifier.fillMaxWidth().testTag("run_permission_allow"),
        )
        RunOutlineButton(
            text = stringResource(R.string.run_permission_settings),
            onClick = { onAction(RunAction.OpenAppSettingsClicked) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** P1-b 배터리 최적화 안내 (처음 한 번) */
@Composable
internal fun RunBatteryContent(
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrepareLayout(
        title = stringResource(R.string.run_battery_title),
        body = stringResource(R.string.run_battery_body),
        modifier = modifier,
    ) {
        RunPrimaryButton(
            text = stringResource(R.string.run_battery_open),
            onClick = { onAction(RunAction.BatterySettingsClicked) },
            modifier = Modifier.fillMaxWidth().testTag("run_battery_open"),
        )
        RunSecondaryButton(
            text = stringResource(R.string.run_battery_later),
            onClick = { onAction(RunAction.BatteryLaterClicked) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** P1-c GPS 대기. 정확한 신호(GOOD)일 때 "시작" 활성, "신호 없이 시작"은 항상 가능 */
@Composable
internal fun RunGpsContent(
    state: RunState,
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        when (state.gps) {
            GpsStatus.GOOD -> R.string.run_gps_title_good
            GpsStatus.WEAK -> R.string.run_gps_title_weak
            GpsStatus.LOST -> R.string.run_gps_title_lost
        }
    PrepareLayout(
        title = stringResource(title),
        body = null,
        modifier = modifier,
        info = if (state.locationEnabled) null else stringResource(R.string.run_gps_location_off),
        header = { RunGpsIndicator(status = state.gps) },
    ) {
        if (!state.locationEnabled) {
            RunSecondaryButton(
                text = stringResource(R.string.run_gps_turn_on),
                onClick = { onAction(RunAction.TurnOnLocationClicked) },
                modifier = Modifier.fillMaxWidth().testTag("run_gps_turn_on"),
            )
        }
        RunPrimaryButton(
            text = stringResource(R.string.run_gps_start),
            onClick = { onAction(RunAction.StartClicked) },
            modifier = Modifier.fillMaxWidth().testTag("run_gps_start"),
            enabled = state.gps == GpsStatus.GOOD && !state.busy,
        )
        RunOutlineButton(
            text = stringResource(R.string.run_gps_start_without_signal),
            onClick = { onAction(RunAction.StartClicked) },
            modifier = Modifier.fillMaxWidth().testTag("run_gps_start_without_signal"),
            enabled = state.gps != GpsStatus.GOOD && !state.busy,
        )
    }
}

/** P2 카운트다운. 화면을 탭하면 바로 시작 */
@Composable
internal fun RunCountdownContent(
    remaining: Int,
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("run_countdown")
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    onAction(RunAction.CountdownTapped)
                },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = remaining.toString(),
            style = AppThemeTokens.typography.metricHuge,
            color = AppThemeTokens.colors.textPrimary,
        )
        Spacer(Modifier.padding(8.dp))
        Text(
            text = stringResource(R.string.run_countdown_hint),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
        )
    }
}

@Composable
private fun PrepareLayout(
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
    info: String? = null,
    header: @Composable () -> Unit = {},
    buttons: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.padding(top = 48.dp))
        header()
        Text(text = title, style = AppThemeTokens.typography.headlineMd, color = AppThemeTokens.colors.textPrimary)
        body?.let {
            Text(
                text = it,
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textSecondary,
                textAlign = TextAlign.Start,
            )
        }
        info?.let { RunInfoBox(text = it, tone = RunInfoBoxTone.WARNING) }
        Spacer(Modifier.padding(top = 16.dp))
        buttons()
    }
}

@ComponentPreviews
@Composable
private fun RunBatteryContentPreview() {
    AppTheme { RunBatteryContent(onAction = {}) }
}

@ComponentPreviews
@Composable
private fun RunCountdownContentPreview() {
    AppTheme { RunCountdownContent(remaining = 3, onAction = {}) }
}
