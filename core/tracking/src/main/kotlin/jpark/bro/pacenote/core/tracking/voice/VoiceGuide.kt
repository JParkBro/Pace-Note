package jpark.bro.pacenote.core.tracking.voice

import jpark.bro.pacenote.core.domain.tracking.AnnouncementScheduler
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState

/**
 * 기록 하나의 음성 안내. 진행 안내는 달리는 중에만, 자동 일시정지 · 재개는 바뀌는 순간 안내한다.
 * 사용자가 누른 일시정지 · 재개는 화면에서 이미 보이므로 안내하지 않는다.
 */
internal class VoiceGuide(
    private val scheduler: AnnouncementScheduler,
    private val announcer: VoiceAnnouncer,
    private val unit: DistanceUnit,
) {
    private var lastPause = PauseState.RUNNING

    /** 복구로 이어서 달릴 때 이미 지난 간격을 다시 안내하지 않는다. */
    fun skipPassed(metrics: LiveMetrics) {
        scheduler.onProgress(metrics.distanceM, metrics.movingMsAtEmit)
    }

    fun onUpdate(
        pause: PauseState,
        metrics: LiveMetrics,
    ) {
        val autoPauseChanged =
            (lastPause == PauseState.RUNNING && pause == PauseState.AUTO_PAUSED) ||
                (lastPause == PauseState.AUTO_PAUSED && pause == PauseState.RUNNING)
        if (autoPauseChanged) {
            scheduler
                .onAutoPauseChanged(paused = pause == PauseState.AUTO_PAUSED, elapsedMs = metrics.emittedAtElapsedMs)
                ?.let { announcer.announce(it, unit) }
        }
        lastPause = pause
        if (pause == PauseState.RUNNING) {
            scheduler.onProgress(metrics.distanceM, metrics.movingMsAtEmit)?.let { announcer.announce(it, unit) }
        }
    }
}
