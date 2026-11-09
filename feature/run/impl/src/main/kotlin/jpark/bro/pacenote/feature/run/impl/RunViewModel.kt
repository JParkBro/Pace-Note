package jpark.bro.pacenote.feature.run.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * 러닝 흐름 P1 ~ P5 · P8 (product-spec 5-2 ~ 5-4, tracking.md 6-2).
 * 기록 중 시간 · 거리는 계산하지 않고 `RunTracker.state`를 옮기기만 한다.
 *
 * @param recoverRunId 값이 있으면 복구 다이얼로그(P8)부터
 */
class RunViewModel(
    private val recoverRunId: String?,
    private val tracker: RunTracker,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<RunState, RunAction, RunEvent>(RunState()) {
    private var device: RunAction.DeviceChecked? = null
    private var batteryGuideShown = true
    private var countdownSeconds = 0
    private var continuingRunId: String? = null
    private var countdownJob: Job? = null

    init {
        settingsRepository
            .observeProfile()
            .onEach {
                setState { copy(distanceUnit = it.distanceUnit) }
            }.launchIn(viewModelScope)
        settingsRepository
            .observeRunSettings()
            .onEach {
                countdownSeconds = it.countdownSeconds
                setState { copy(keepScreenOn = it.keepScreenOn) }
            }.launchIn(viewModelScope)
        settingsRepository.observeBatteryGuideShown().onEach { batteryGuideShown = it }.launchIn(viewModelScope)
        tracker.state.onEach { tracking -> setState { withTracking(tracking) } }.launchIn(viewModelScope)
        if (recoverRunId != null && tracker.state.value !is TrackingState.Recording) {
            viewModelScope.launch {
                val run = tracker.recoverableRun()?.takeIf { it.runId == recoverRunId } ?: return@launch
                val isOld = clock.millis() - run.startedAt > OLD_RECOVERY.inWholeMilliseconds
                setState { copy(step = RunStep.RECOVER, recoverable = run, isOldRecovery = isOld) }
            }
        }
    }

    override fun onAction(action: RunAction) {
        when (action) {
            is RunAction.DeviceChecked -> onDeviceChecked(action)
            RunAction.RequestPermissionClicked -> sendEvent(RunEvent.RequestLocationPermission)
            RunAction.OpenAppSettingsClicked -> sendEvent(RunEvent.OpenAppSettings)
            RunAction.BatterySettingsClicked -> closeBatteryGuide(openSettings = true)
            RunAction.BatteryLaterClicked -> closeBatteryGuide(openSettings = false)
            RunAction.TurnOnLocationClicked -> sendEvent(RunEvent.RequestLocationSettings)
            RunAction.StartClicked -> onStartClicked()
            RunAction.CountdownTapped -> startRecording()
            RunAction.PauseClicked -> viewModelScope.launch { tracker.pause() }
            RunAction.ResumeClicked -> viewModelScope.launch { tracker.resume() }
            RunAction.LockClicked -> setState { copy(locked = true) }
            RunAction.UnlockLongPressed -> setState { copy(locked = false) }
            RunAction.StopLongPressed -> setState { copy(showFinishSheet = true) }
            RunAction.FinishSheetDismissed -> setState { copy(showFinishSheet = false) }
            RunAction.ContinueClicked -> continueRunning()
            RunAction.SaveClicked -> finish(save = true)
            RunAction.DiscardClicked -> finish(save = false)
            RunAction.RecoverContinueClicked -> continueRecovered()
            RunAction.RecoverSaveClicked -> finishRecovered(save = true)
            RunAction.RecoverDiscardClicked -> finishRecovered(save = false)
            RunAction.BackPressed -> onBack()
        }
    }

    /** 권한 → 배터리 안내(처음 한 번) → GPS 대기 순서로 진행한다 (product-spec 5-2). */
    private fun onDeviceChecked(checked: RunAction.DeviceChecked) {
        device = checked
        setState { copy(locationEnabled = checked.locationEnabled) }
        if (currentState.step != RunStep.CHECKING && currentState.step != RunStep.PERMISSION) return
        val next = preparationStep(checked, batteryGuideShown)
        setState { copy(step = next, coarseOnly = !checked.fineLocationGranted && checked.coarseLocationGranted) }
        if (next == RunStep.GPS) viewModelScope.launch { tracker.prepare() }
    }

    private fun closeBatteryGuide(openSettings: Boolean) {
        viewModelScope.launch { settingsRepository.setBatteryGuideShown(true) }
        batteryGuideShown = true
        if (openSettings) sendEvent(RunEvent.OpenBatterySettings)
        setState { copy(step = RunStep.GPS) }
        viewModelScope.launch { tracker.prepare() }
    }

    private fun onStartClicked() {
        if (currentState.step != RunStep.GPS) return
        // 이어서 달리기는 카운트다운 없이 바로 이어간다 (tracking.md 6-2)
        if (continuingRunId != null || countdownSeconds == 0) return startRecording()
        setState { copy(step = RunStep.COUNTDOWN, countdownRemaining = countdownSeconds) }
        countdownJob =
            viewModelScope.launch {
                while (currentState.countdownRemaining > 0) {
                    delay(1.seconds)
                    setState { copy(countdownRemaining = countdownRemaining - 1) }
                }
                startRecording()
            }
    }

    private fun startRecording() {
        countdownJob?.cancel()
        countdownJob = null
        if (currentState.busy) return
        setState { copy(busy = true) }
        viewModelScope.launch {
            tracker
                .start(continuingRunId)
                .onFailure {
                    setState { copy(step = RunStep.GPS) }
                    sendEvent(RunEvent.ShowError)
                }
            setState { copy(busy = false) }
        }
    }

    private fun continueRunning() {
        setState { copy(showFinishSheet = false) }
        viewModelScope.launch { tracker.resume() }
    }

    private fun finish(save: Boolean) {
        setState { copy(showFinishSheet = false, busy = true) }
        viewModelScope.launch {
            tracker
                .finish(save)
                .onSuccess { runId -> sendEvent(runId?.let(RunEvent::OpenResult) ?: RunEvent.Close) }
                .onFailure {
                    setState { copy(step = RunStep.RECORDING) }
                    sendEvent(RunEvent.ShowError)
                }
            setState { copy(busy = false) }
        }
    }

    private fun continueRecovered() {
        continuingRunId = currentState.recoverable?.runId ?: return
        setState { copy(step = RunStep.CHECKING) }
        // 기기 상태를 아직 못 받았으면 Root가 알려줄 때 진행한다
        device?.let(::onDeviceChecked)
    }

    private fun finishRecovered(save: Boolean) {
        val runId = currentState.recoverable?.runId ?: return
        setState { copy(busy = true) }
        viewModelScope.launch {
            tracker
                .finishRecovered(runId, save)
                .onSuccess { sendEvent(if (save) RunEvent.OpenResult(runId) else RunEvent.Close) }
                .onFailure { sendEvent(RunEvent.ShowError) }
            setState { copy(busy = false) }
        }
    }

    private fun onBack() {
        val step = currentState.step
        when {
            step == RunStep.RECORDING && currentState.showFinishSheet -> {
                setState { copy(showFinishSheet = false) }
            }

            step == RunStep.RECORDING -> {
                sendEvent(RunEvent.MoveToBackground)
            }

            step == RunStep.COUNTDOWN -> {
                countdownJob?.cancel()
                setState { copy(step = RunStep.GPS, countdownRemaining = 0) }
            }

            step == RunStep.RECOVER -> {
                sendEvent(RunEvent.Close)
            }

            // 저장 중에는 뒤로 가기를 무시한다
            step == RunStep.FINISHING -> {}

            else -> {
                viewModelScope.launch { tracker.cancelPreparing() }
                sendEvent(RunEvent.Close)
            }
        }
    }

    private companion object {
        val OLD_RECOVERY = 24.hours
    }
}

private fun preparationStep(
    checked: RunAction.DeviceChecked,
    batteryGuideShown: Boolean,
): RunStep =
    when {
        !checked.fineLocationGranted -> RunStep.PERMISSION
        !batteryGuideShown && !checked.ignoringBatteryOptimizations -> RunStep.BATTERY
        else -> RunStep.GPS
    }

/** 추적 상태를 화면 상태로 옮긴다. 저장 중에는 단계를 바꾸지 않는다. */
private fun RunState.withTracking(tracking: TrackingState): RunState =
    when (tracking) {
        is TrackingState.Recording -> {
            copy(
                step =
                    if (step ==
                        RunStep.FINISHING
                    ) {
                        step
                    } else {
                        RunStep.RECORDING
                    },
                recording = tracking,
                gps = tracking.gps,
            )
        }

        is TrackingState.Preparing -> {
            copy(gps = tracking.gps)
        }

        is TrackingState.Finishing -> {
            copy(step = RunStep.FINISHING)
        }

        TrackingState.Idle -> {
            this
        }
    }
