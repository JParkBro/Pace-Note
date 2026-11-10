package jpark.bro.pacenote.core.tracking

import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.domain.tracking.AnnouncementScheduler
import jpark.bro.pacenote.core.domain.tracking.GpsStatusEvaluator
import jpark.bro.pacenote.core.domain.tracking.RunMetricsAccumulator
import jpark.bro.pacenote.core.location.LocationSource
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.tracking.voice.VoiceAnnouncer
import jpark.bro.pacenote.core.tracking.voice.VoiceGuide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 추적 상태 머신 (tracking.md 2 · 3 · 6장). 위치 점과 1초 틱을 받아 계산 · 저장 · 상태 갱신을 한다.
 * 모든 변경은 [mutex] 안에서 순서대로 일어나 저장 경합이 없다. Android 서비스는 모른다.
 */
internal class TrackingEngine(
    private val locationSource: LocationSource,
    private val recorder: RunRecorder,
    private val settingsRepository: SettingsRepository,
    private val voiceAnnouncer: VoiceAnnouncer,
    private val elapsedClock: ElapsedClock,
    private val scope: CoroutineScope,
) : RunTracker {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<TrackingState>(TrackingState.Idle)
    override val state: StateFlow<TrackingState> = mutableState.asStateFlow()

    private var sensingJob: Job? = null
    private var gps = GpsStatusEvaluator()
    private var session: RecordingSession? = null

    private val nowMs: Long get() = elapsedClock.elapsedRealtimeMs()

    override suspend fun prepare() =
        mutex.withLock {
            if (mutableState.value != TrackingState.Idle) return@withLock
            gps = GpsStatusEvaluator()
            mutableState.value = TrackingState.Preparing(gps.status(nowMs))
            startSensing()
        }

    override suspend fun cancelPreparing() =
        mutex.withLock {
            if (mutableState.value !is TrackingState.Preparing) return@withLock
            sensingJob?.cancel()
            mutableState.value = TrackingState.Idle
        }

    override suspend fun start(recoverRunId: String?): Result<String> =
        mutex.withLock {
            if (mutableState.value !is TrackingState.Preparing) return@withLock notAllowed("start", mutableState.value)
            suspendRunCatching {
                val now = nowMs
                val settings = settingsRepository.observeRunSettings().first()
                val resumed = recoverRunId?.let { recorder.resume(it, now) }
                val runId = resumed?.runId ?: recorder.create(now)
                val accumulator = RunMetricsAccumulator(now, settings.autoPauseEnabled, resumed?.progress)
                val voice =
                    if (settings.voiceEnabled) {
                        voiceAnnouncer.prepare()
                        val unit = settingsRepository.observeProfile().first().distanceUnit
                        VoiceGuide(
                            AnnouncementScheduler(settings.voiceInterval, settings.voiceItems),
                            voiceAnnouncer,
                            unit,
                        ).also { it.skipPassed(accumulator.metrics(now)) }
                    } else {
                        null
                    }
                val current = RecordingSession(runId, accumulator, recorder, resumed?.route.orEmpty(), voice)
                session = current
                mutableState.value = current.snapshot(now, gps.status(now))
                current.runId
            }
        }

    override suspend fun pause() =
        mutex.withLock {
            val current = session ?: return@withLock
            if (current.pauseState == PauseState.USER_PAUSED) return@withLock
            val now = nowMs
            suspendRunCatching { current.pause(now) }
            mutableState.value = current.snapshot(now, gps.status(now))
        }

    override suspend fun resume() =
        mutex.withLock {
            val current = session ?: return@withLock
            if (current.pauseState != PauseState.USER_PAUSED) return@withLock
            val now = nowMs
            suspendRunCatching { current.resume(now) }
            mutableState.value = current.snapshot(now, gps.status(now))
        }

    override suspend fun finish(save: Boolean): Result<String?> =
        mutex.withLock {
            val current = session ?: return@withLock notAllowed("finish", mutableState.value)
            mutableState.value = TrackingState.Finishing(current.runId)
            sensingJob?.cancelAndJoin()
            val now = nowMs
            val weightKg = settingsRepository.observeProfile().first().weightKg
            suspendRunCatching { current.finish(save, weightKg, now) }
                .onSuccess {
                    session = null
                    voiceAnnouncer.release()
                    mutableState.value = TrackingState.Idle
                }.onFailure {
                    // 저장에 실패하면 기록을 이어간다. 버퍼는 남아 있어 다음 저장 때 다시 쓴다
                    startSensing()
                    mutableState.value = current.snapshot(now, gps.status(now))
                }.map { current.runId.takeIf { save } }
        }

    override suspend fun recoverableRun(): RecoverableRun? =
        mutex.withLock { if (session == null) recorder.findInProgress() else null }

    override suspend fun finishRecovered(
        runId: String,
        save: Boolean,
    ): Result<Unit> =
        mutex.withLock {
            if (mutableState.value !=
                TrackingState.Idle
            ) {
                return@withLock notAllowed("finishRecovered", mutableState.value)
            }
            suspendRunCatching {
                recorder.finishRecovered(runId, save, settingsRepository.observeProfile().first().weightKg)
            }
        }

    /** 위치 점과 1초 틱을 한 흐름으로 받는다. 위치 요청이 실패하면 점 없이 틱만 와서 GPS가 LOST가 된다. */
    private fun startSensing() {
        val points: Flow<RawLocation?> = locationSource.locations().catch { }
        val ticks: Flow<RawLocation?> =
            flow {
                while (true) {
                    delay(TICK_MS)
                    emit(null)
                }
            }
        sensingJob =
            scope.launch {
                merge(points, ticks).collect { point -> mutex.withLock { onSignal(point) } }
            }
    }

    private suspend fun onSignal(point: RawLocation?) {
        val now = nowMs
        point?.let(gps::onPoint)
        val current = session
        if (current == null) {
            mutableState.value = TrackingState.Preparing(gps.status(now))
            return
        }
        // 쓰기 실패는 다음 저장 때 다시 시도한다. 계산과 상태 갱신은 멈추지 않는다
        suspendRunCatching { if (point != null) current.onPoint(point, now) else current.onTick(now) }
        mutableState.value = current.snapshot(now, gps.status(now))
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}

private fun <T> notAllowed(
    action: String,
    state: TrackingState,
): Result<T> = Result.failure(IllegalStateException("$action: 지금 상태(${state::class.simpleName})에서 할 수 없음"))
