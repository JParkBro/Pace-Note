package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.VoiceAnnouncement
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem

/** 음성 안내 시점을 판단한다 (docs/run/tracking.md 5-1 · 9장). */
class AnnouncementScheduler(
    private val interval: VoiceInterval,
    private val items: Set<VoiceItem>,
) {
    private var passedIntervals = 0L
    private var lastDistanceM = 0.0
    private var lastMovingMs = 0L
    private var lastAutoPauseChangeMs: Long? = null

    /** 간격을 넘었으면 안내할 내용. 신호 공백 뒤처럼 여러 간격을 한 번에 넘어도 한 번만 안내한다. */
    fun onProgress(
        distanceM: Double,
        movingMs: Long,
    ): VoiceAnnouncement.Progress? {
        val passed = passedIntervalsAt(distanceM, movingMs)
        if (passed <= passedIntervals) return null
        passedIntervals = passed
        val announcement =
            VoiceAnnouncement.Progress(
                distanceM = interval.distanceM?.let { it * passed } ?: distanceM,
                movingMs = interval.durationMs?.let { it * passed } ?: movingMs,
                averagePaceSecPerKm = paceSecPerKm(movingMs, distanceM),
                splitPaceSecPerKm = paceSecPerKm(movingMs - lastMovingMs, distanceM - lastDistanceM),
                items = items,
            )
        lastDistanceM = distanceM
        lastMovingMs = movingMs
        return announcement
    }

    /** 신호등 앞에서 서성일 때처럼 10초 안에 반복되면 null */
    fun onAutoPauseChanged(
        paused: Boolean,
        elapsedMs: Long,
    ): VoiceAnnouncement? {
        val repeated = lastAutoPauseChangeMs?.let { elapsedMs - it < REPEAT_WINDOW_MS } ?: false
        lastAutoPauseChangeMs = elapsedMs
        return when {
            repeated -> null
            paused -> VoiceAnnouncement.AutoPaused
            else -> VoiceAnnouncement.AutoResumed
        }
    }

    private fun passedIntervalsAt(
        distanceM: Double,
        movingMs: Long,
    ): Long = interval.distanceM?.let { (distanceM / it).toLong() } ?: (movingMs / checkNotNull(interval.durationMs))

    private fun paceSecPerKm(
        movingMs: Long,
        distanceM: Double,
    ): Double? = (movingMs / MS_PER_SECOND / (distanceM / METERS_PER_KM)).takeIf { distanceM >= MIN_PACE_DISTANCE_M }

    private companion object {
        const val REPEAT_WINDOW_MS = 10_000L
        const val MIN_PACE_DISTANCE_M = 50.0
        const val MS_PER_SECOND = 1_000.0
        const val METERS_PER_KM = 1_000.0
    }
}
