package jpark.bro.pacenote.feature.run.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.run.impl.ui.KeepScreenOn
import jpark.bro.pacenote.feature.run.impl.ui.RunBatteryContent
import jpark.bro.pacenote.feature.run.impl.ui.RunCountdownContent
import jpark.bro.pacenote.feature.run.impl.ui.RunFinishSheet
import jpark.bro.pacenote.feature.run.impl.ui.RunGpsContent
import jpark.bro.pacenote.feature.run.impl.ui.RunLoadingContent
import jpark.bro.pacenote.feature.run.impl.ui.RunPermissionContent
import jpark.bro.pacenote.feature.run.impl.ui.RunRecordingContent
import jpark.bro.pacenote.feature.run.impl.ui.RunRecoverDialog
import jpark.bro.pacenote.feature.run.impl.ui.deviceChecked
import jpark.bro.pacenote.feature.run.impl.ui.previewRecording
import jpark.bro.pacenote.feature.run.impl.ui.requestLocationSettings
import jpark.bro.pacenote.feature.run.impl.ui.requiredPermissions
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import java.time.Clock
import java.time.ZoneId

private const val TICK_MS = 1_000L

// 미리보기: 2026.10.28 (수) 오전 6:40 시작, 3.42km, 20:31
private const val PREVIEW_STARTED_AT = 1_793_137_200_000L
private const val PREVIEW_DISTANCE_M = 3_420.0
private const val PREVIEW_MOVING_MS = 1_231_000L

/**
 * 러닝 흐름의 Root: ViewModel 연결, 권한 · 설정 · 위치 켜기 요청, 뒤로 가기, 화면 켜짐 유지.
 * 기기 상태(권한 · 위치 · 배터리 최적화)는 화면에 돌아올 때마다 다시 확인해 ViewModel에 알린다.
 */
@Composable
internal fun RunRoot(
    onClose: () -> Unit,
    onOpenResult: (runId: String) -> Unit,
    viewModel: RunViewModel,
    elapsedClock: ElapsedClock = koinInject(),
    clock: Clock = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.run_error)
    val checkDevice = { viewModel.onAction(context.deviceChecked()) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { checkDevice() }
    val locationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { checkDevice() }

    LifecycleResumeEffect(Unit) {
        checkDevice()
        onPauseOrDispose {}
    }
    BackHandler { viewModel.onAction(RunAction.BackPressed) }
    KeepScreenOn(state.keepScreenOn && state.step == RunStep.RECORDING)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            RunEvent.RequestLocationPermission -> {
                permissionLauncher.launch(requiredPermissions())
            }

            RunEvent.OpenAppSettings -> {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            }

            RunEvent.OpenBatterySettings -> {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }

            RunEvent.RequestLocationSettings -> {
                requestLocationSettings(context) { locationLauncher.launch(IntentSenderRequest.Builder(it).build()) }
            }

            is RunEvent.OpenResult -> {
                onOpenResult(event.runId)
            }

            RunEvent.Close -> {
                onClose()
            }

            RunEvent.MoveToBackground -> {
                activity?.moveTaskToBack(true)
            }

            RunEvent.ShowError -> {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    val nowElapsedMs by produceState(elapsedClock.elapsedRealtimeMs()) {
        while (true) {
            value = elapsedClock.elapsedRealtimeMs()
            delay(TICK_MS)
        }
    }
    RunScreen(state, nowElapsedMs, clock.zone, snackbarHostState, viewModel::onAction)
}

@Composable
private fun RunScreen(
    state: RunState,
    nowElapsedMs: Long,
    zoneId: ZoneId,
    snackbarHostState: SnackbarHostState,
    onAction: (RunAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize(), snackbarHost = { RunToastHost(snackbarHostState) }) { padding ->
        val content = Modifier.padding(padding)
        val recording = state.recording
        when (state.step) {
            RunStep.CHECKING, RunStep.FINISHING, RunStep.RECOVER -> {
                RunLoadingContent(content)
            }

            RunStep.PERMISSION -> {
                RunPermissionContent(state, onAction, content)
            }

            RunStep.BATTERY -> {
                RunBatteryContent(onAction, content)
            }

            RunStep.GPS -> {
                RunGpsContent(state, onAction, content)
            }

            RunStep.COUNTDOWN -> {
                RunCountdownContent(state.countdownRemaining, onAction, content)
            }

            RunStep.RECORDING -> {
                if (recording != null) {
                    RunRecordingContent(recording, state.distanceUnit, state.locked, nowElapsedMs, onAction, content)
                } else {
                    RunLoadingContent(content)
                }
            }
        }
        if (state.step == RunStep.RECORDING && state.showFinishSheet && recording != null) {
            RunFinishSheet(recording.metrics.distanceM, recording.metrics.movingMsAtEmit, state.distanceUnit, onAction)
        }
        val recoverable = state.recoverable
        if (state.step == RunStep.RECOVER && recoverable != null) {
            RunRecoverDialog(recoverable, state.isOldRecovery, state.distanceUnit, zoneId, onAction)
        }
    }
}

@Composable
private fun Preview(
    state: RunState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) {
        RunScreen(state, 0L, ZoneId.of("Asia/Seoul"), remember { SnackbarHostState() }, {})
    }
}

@ScreenPreviews
@Composable
private fun RunRecordingPreview() = Preview(RunState(step = RunStep.RECORDING, recording = previewRecording()))

@ScreenPreviews
@Composable
private fun RunRecordingInkDarkPreview() =
    Preview(RunState(step = RunStep.RECORDING, recording = previewRecording()), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun RunPausedPreview() =
    Preview(RunState(step = RunStep.RECORDING, recording = previewRecording(PauseState.USER_PAUSED)))

@ScreenPreviews
@Composable
private fun RunLockedPreview() =
    Preview(RunState(step = RunStep.RECORDING, recording = previewRecording(), locked = true))

@ScreenPreviews
@Composable
private fun RunGpsPreview() = Preview(RunState(step = RunStep.GPS, gps = GpsStatus.WEAK, locationEnabled = false))

@ScreenPreviews
@Composable
private fun RunPermissionPreview() = Preview(RunState(step = RunStep.PERMISSION, coarseOnly = true))

@ScreenPreviews
@Composable
private fun RunRecoverPreview() =
    Preview(
        RunState(
            step = RunStep.RECOVER,
            recoverable = RecoverableRun("run-preview", PREVIEW_STARTED_AT, PREVIEW_DISTANCE_M, PREVIEW_MOVING_MS),
            isOldRecovery = true,
        ),
    )
