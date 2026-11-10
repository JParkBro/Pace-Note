package jpark.bro.pacenote.feature.run.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.designsystem.component.RunGpsIndicator
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunPausedPill
import jpark.bro.pacenote.core.designsystem.component.RunRoundButton
import jpark.bro.pacenote.core.designsystem.component.RunRoundButtonKind
import jpark.bro.pacenote.core.designsystem.component.RunStatSize
import jpark.bro.pacenote.core.designsystem.component.RunStatTile
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.run.impl.R
import jpark.bro.pacenote.feature.run.impl.RunAction
import jpark.bro.pacenote.core.designsystem.R as DesignR

private const val ROUTE_ASPECT = 1.6f
private const val ROUTE_PADDING = 0.1f

/**
 * P3 러닝 중 · P3-L 잠금 · P4 일시정지. 수치는 추적 상태를 옮긴 값이고, 시간만 [nowElapsedMs]로 보간한다 (tracking.md 3장).
 */
@Composable
internal fun RunRecordingContent(
    recording: TrackingState.Recording,
    unit: DistanceUnit,
    locked: Boolean,
    nowElapsedMs: Long,
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val paused = recording.pause == PauseState.USER_PAUSED
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState(),
                ).padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!locked) {
                RunIconButton(
                    icon = DesignR.drawable.ic_lock,
                    contentDescription = stringResource(R.string.run_recording_lock),
                    onClick = { onAction(RunAction.LockClicked) },
                    modifier = Modifier.align(Alignment.CenterEnd).testTag("run_lock"),
                )
            }
        }
        PauseLabel(recording.pause)
        Metrics(recording, unit, nowElapsedMs)
        LiveRoute(recording)
        Controls(paused = paused, locked = locked, onAction = onAction)
    }
}

@Composable
private fun PauseLabel(pause: PauseState) {
    when (pause) {
        PauseState.USER_PAUSED -> RunPausedPill(text = stringResource(R.string.run_recording_paused))
        PauseState.AUTO_PAUSED -> RunPausedPill(text = stringResource(R.string.run_recording_auto_paused))
        PauseState.RUNNING -> Unit
    }
}

@Composable
private fun Metrics(
    recording: TrackingState.Recording,
    unit: DistanceUnit,
    nowElapsedMs: Long,
) {
    val metrics = recording.metrics
    val running = recording.pause == PauseState.RUNNING
    val movingMs =
        metrics.movingMsAtEmit + if (running) (nowElapsedMs - metrics.emittedAtElapsedMs).coerceAtLeast(0) else 0L
    // 일시정지(P4)는 평균 페이스, 달리는 중은 현재 페이스
    val showAverage = recording.pause == PauseState.USER_PAUSED
    val pace = if (showAverage) metrics.avgPaceSecPerKm else metrics.currentPaceSecPerKm
    RunStatTile(
        value = RunFormat.distance(metrics.distanceM, unit),
        label = stringResource(R.string.run_recording_distance, stringResource(RunFormat.unitLabelRes(unit))),
        size = RunStatSize.HUGE,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("run_distance"),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        RunStatTile(
            value = RunFormat.duration(movingMs),
            label = stringResource(R.string.run_recording_time),
            size = RunStatSize.LARGE,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("run_time"),
        )
        RunStatTile(
            value = RunFormat.pace(pace, unit),
            label =
                stringResource(
                    if (showAverage) R.string.run_recording_average_pace else R.string.run_recording_current_pace,
                ),
            size = RunStatSize.LARGE,
            hasValue = pace != null,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
    }
}

@Composable
private fun LiveRoute(recording: TrackingState.Recording) {
    val projected = remember(recording.route) { RouteProjector.project(recording.route, ROUTE_ASPECT, ROUTE_PADDING) }
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(ROUTE_ASPECT)) {
        RouteCanvas(
            route = projected,
            style = RouteCanvasStyle.LIVE,
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(R.string.run_recording_route),
        )
        RunGpsIndicator(status = recording.gps, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

@Composable
private fun Controls(
    paused: Boolean,
    locked: Boolean,
    onAction: (RunAction) -> Unit,
) {
    when {
        locked -> {
            ControlWithHint(R.string.run_recording_unlock) {
                RunRoundButton(
                    icon = DesignR.drawable.ic_lock_open,
                    contentDescription = stringResource(R.string.run_recording_unlock),
                    onClick = { onAction(RunAction.UnlockLongPressed) },
                    kind = RunRoundButtonKind.LOCK,
                    longPress = true,
                    modifier = Modifier.testTag("run_unlock"),
                )
            }
        }

        paused -> {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                RunRoundButton(
                    icon = DesignR.drawable.ic_play_arrow,
                    contentDescription = stringResource(R.string.run_recording_resume),
                    onClick = { onAction(RunAction.ResumeClicked) },
                    modifier = Modifier.testTag("run_resume"),
                )
                RunRoundButton(
                    icon = DesignR.drawable.ic_stop,
                    contentDescription = stringResource(R.string.run_recording_stop),
                    onClick = { onAction(RunAction.StopLongPressed) },
                    kind = RunRoundButtonKind.NEUTRAL,
                    longPress = true,
                    modifier = Modifier.testTag("run_stop"),
                )
            }
        }

        else -> {
            ControlWithHint(R.string.run_recording_pause_hint) {
                RunRoundButton(
                    icon = DesignR.drawable.ic_pause,
                    contentDescription = stringResource(R.string.run_recording_pause),
                    onClick = { onAction(RunAction.PauseClicked) },
                    modifier = Modifier.testTag("run_pause"),
                )
            }
        }
    }
}

@Composable
private fun ControlWithHint(
    hint: Int,
    button: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        button()
        Text(
            text = stringResource(hint),
            style = AppThemeTokens.typography.bodySm,
            color = AppThemeTokens.colors.textSecondary,
        )
    }
}
