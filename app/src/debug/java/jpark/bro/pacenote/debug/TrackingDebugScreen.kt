package jpark.bro.pacenote.debug

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import jpark.bro.pacenote.R
import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.RunFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val TICK_MS = 1_000L
private const val REPLAY_FAST = 10.0

/** debug 빌드 전용 추적 테스트 화면. 실기기 확인(tracking.md 8-3)과 GPX 재생에 쓴다. 출시 화면이 아니다. */
@Composable
fun TrackingDebugScreen(
    tracker: RunTracker = koinInject(),
    source: DebugLocationSource = koinInject(),
    elapsedClock: ElapsedClock = koinInject(),
) {
    val state by tracker.state.collectAsState()
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("–") }
    var recoverable by remember { mutableStateOf<RecoverableRun?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(state, refresh) { recoverable = tracker.recoverableRun() }
    val act: (suspend () -> Any?) -> Unit = { action ->
        scope.launch {
            result = action().toString()
            refresh++
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.debug_tracking_title), style = MaterialTheme.typography.titleLarge)
            PermissionSection(source)
            SourceSection(source)
            StateSection(state, elapsedClock)
            ControlSection(state, tracker, act)
            recoverable?.let { RecoverySection(it, state, tracker, act) }
            Text(stringResource(R.string.debug_tracking_result, result))
        }
    }
}

@Composable
private fun PermissionSection(source: DebugLocationSource) {
    val context = LocalContext.current
    var checked by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { checked++ }
    val on = stringResource(R.string.debug_tracking_on)
    val off = stringResource(R.string.debug_tracking_off)
    val status =
        remember(checked) {
            Triple(
                context.granted(Manifest.permission.ACCESS_FINE_LOCATION),
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    context.granted(Manifest.permission.POST_NOTIFICATIONS),
                source.isLocationEnabled(),
            )
        }
    Text(
        stringResource(
            R.string.debug_tracking_permission_status,
            if (status.first) on else off,
            if (status.second) on else off,
            if (status.third) on else off,
        ),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
            )
        }) { Text(stringResource(R.string.debug_tracking_permissions)) }
        OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }) { Text(stringResource(R.string.debug_tracking_battery)) }
    }
}

@Composable
private fun SourceSection(source: DebugLocationSource) {
    val speed by source.replaySpeed.collectAsState()
    Text(stringResource(R.string.debug_tracking_source))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            null to R.string.debug_tracking_source_device,
            1.0 to R.string.debug_tracking_source_replay_1x,
            REPLAY_FAST to R.string.debug_tracking_source_replay_10x,
        ).forEach { (value, label) ->
            FilterChip(
                selected = speed == value,
                onClick = { source.replaySpeed.value = value },
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
private fun StateSection(
    state: TrackingState,
    elapsedClock: ElapsedClock,
) {
    var now by remember { mutableLongStateOf(elapsedClock.elapsedRealtimeMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = elapsedClock.elapsedRealtimeMs()
            delay(TICK_MS)
        }
    }
    Text(stringResource(R.string.debug_tracking_state, state::class.simpleName.orEmpty()))
    when (state) {
        is TrackingState.Preparing -> {
            Text(stringResource(R.string.debug_tracking_gps, state.gps.name))
        }

        is TrackingState.Recording -> {
            RecordingText(state, now)
        }

        TrackingState.Idle, is TrackingState.Finishing -> {}
    }
}

@Composable
private fun RecordingText(
    state: TrackingState.Recording,
    nowElapsedMs: Long,
) {
    val metrics = state.metrics
    // 시계 보간: 마지막 상태 이후 흐른 시간을 더한다 (tracking.md 3장)
    val moving =
        metrics.movingMsAtEmit +
            if (state.pause == PauseState.RUNNING) nowElapsedMs - metrics.emittedAtElapsedMs else 0L
    Text(
        stringResource(
            R.string.debug_tracking_metrics,
            RunFormat.distance(metrics.distanceM, DistanceUnit.KM),
            RunFormat.duration(moving),
            RunFormat.pace(metrics.currentPaceSecPerKm, DistanceUnit.KM),
            RunFormat.pace(metrics.avgPaceSecPerKm, DistanceUnit.KM),
        ),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(stringResource(R.string.debug_tracking_gps, state.gps.name))
    Text(
        stringResource(
            R.string.debug_tracking_route,
            state.pause.name,
            state.route.size,
            state.route.lastOrNull()?.segment ?: 0,
        ),
    )
}

@Composable
private fun ControlSection(
    state: TrackingState,
    tracker: RunTracker,
    act: (suspend () -> Any?) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state) {
            TrackingState.Idle -> {
                Button(
                    onClick = { act { tracker.prepare() } },
                ) { Text(stringResource(R.string.debug_tracking_prepare)) }
            }

            is TrackingState.Preparing -> {
                Button(onClick = { act { tracker.start() } }) { Text(stringResource(R.string.debug_tracking_start)) }
                OutlinedButton(
                    onClick = { act { tracker.cancelPreparing() } },
                ) { Text(stringResource(R.string.debug_tracking_cancel)) }
            }

            is TrackingState.Recording -> {
                if (state.pause == PauseState.USER_PAUSED) {
                    Button(
                        onClick = { act { tracker.resume() } },
                    ) { Text(stringResource(R.string.debug_tracking_resume)) }
                } else {
                    Button(
                        onClick = { act { tracker.pause() } },
                    ) { Text(stringResource(R.string.debug_tracking_pause)) }
                }
                OutlinedButton(onClick = { act { tracker.finish(save = true) } }) {
                    Text(stringResource(R.string.debug_tracking_finish_save))
                }
                OutlinedButton(onClick = { act { tracker.finish(save = false) } }) {
                    Text(stringResource(R.string.debug_tracking_finish_discard))
                }
            }

            is TrackingState.Finishing -> {}
        }
    }
}

@Composable
private fun RecoverySection(
    run: RecoverableRun,
    state: TrackingState,
    tracker: RunTracker,
    act: (suspend () -> Any?) -> Unit,
) {
    Text(
        stringResource(
            R.string.debug_tracking_recoverable,
            run.runId.take(8),
            RunFormat.distance(run.distanceM, DistanceUnit.KM),
            RunFormat.duration(run.movingMs),
        ),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state is TrackingState.Preparing) {
            Button(onClick = { act { tracker.start(recoverRunId = run.runId) } }) {
                Text(stringResource(R.string.debug_tracking_recover_continue))
            }
        }
        if (state == TrackingState.Idle) {
            OutlinedButton(onClick = { act { tracker.finishRecovered(run.runId, save = true) } }) {
                Text(stringResource(R.string.debug_tracking_recover_save))
            }
            OutlinedButton(onClick = { act { tracker.finishRecovered(run.runId, save = false) } }) {
                Text(stringResource(R.string.debug_tracking_recover_discard))
            }
        }
    }
}

private fun Context.granted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
