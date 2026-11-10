package jpark.bro.pacenote.core.tracking.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 위치 타입 포그라운드 서비스 껍데기 (tracking.md 7장). 알림 유지 · 생명주기만 맡고 로직은 `TrackingEngine`에 있다.
 * 프로세스가 죽으면 다시 띄우지 않는다(START_NOT_STICKY). 사용자가 앱을 열 때 복구(P8)한다.
 */
class RunTrackingService :
    LifecycleService(),
    KoinComponent {
    private val tracker: RunTracker by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val elapsedClock: ElapsedClock by inject()
    private lateinit var notification: TrackingNotification
    private var observing: Job? = null
    private var lastNotifiedElapsedMs = Long.MIN_VALUE
    private var lastNotifiedPause: PauseState? = null

    override fun onCreate() {
        super.onCreate()
        notification = TrackingNotification(this).also { it.createChannel() }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        // startForegroundService() 뒤에는 무엇보다 먼저 startForeground()를 부른다 (tracking.md 7-2)
        val current = tracker.state.value as? TrackingState.Recording
        ServiceCompat.startForeground(
            this,
            TrackingNotification.NOTIFICATION_ID,
            notification.build(current, DistanceUnit.KM, current?.let(::movingNow) ?: 0L),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )
        when (intent?.action) {
            ACTION_PAUSE -> lifecycleScope.launch { tracker.pause() }
            ACTION_RESUME -> lifecycleScope.launch { tracker.resume() }
        }
        observe()
        return START_NOT_STICKY
    }

    private fun observe() {
        if (observing != null) return
        val units = settingsRepository.observeProfile().map { it.distanceUnit }
        observing =
            lifecycleScope.launch {
                combine(tracker.state, units, ::Pair).collect { (state, unit) ->
                    when (state) {
                        is TrackingState.Recording -> updateIfDue(state, unit)
                        TrackingState.Idle -> stop()
                        is TrackingState.Preparing, is TrackingState.Finishing -> Unit
                    }
                }
            }
    }

    /** 거리 · 페이스는 5초마다, 일시정지 · 재개는 바로 반영한다. */
    private fun updateIfDue(
        state: TrackingState.Recording,
        unit: DistanceUnit,
    ) {
        val now = elapsedClock.elapsedRealtimeMs()
        if (state.pause == lastNotifiedPause && now - lastNotifiedElapsedMs < UPDATE_INTERVAL_MS) return
        lastNotifiedElapsedMs = now
        lastNotifiedPause = state.pause
        notification.notify(notification.build(state, unit, movingNow(state)))
    }

    private fun movingNow(state: TrackingState.Recording): Long {
        val sinceEmit =
            if (state.pause ==
                PauseState.RUNNING
            ) {
                elapsedClock.elapsedRealtimeMs() - state.metrics.emittedAtElapsedMs
            } else {
                0L
            }
        return state.metrics.movingMsAtEmit + sinceEmit.coerceAtLeast(0)
    }

    private fun stop() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        internal const val ACTION_PAUSE = "jpark.bro.pacenote.tracking.PAUSE"
        internal const val ACTION_RESUME = "jpark.bro.pacenote.tracking.RESUME"
        private const val UPDATE_INTERVAL_MS = 5_000L

        /** 앱이 화면에 보이는 상태에서 사용자 동작(카운트다운 끝 · 이어서 달리기)으로만 부른다 */
        internal fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, RunTrackingService::class.java))
        }
    }
}
