package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation

enum class AutoPauseTransition {
    PAUSE,
    RESUME,
}

/** 위치 차이로 정지와 재개를 판단한다 (docs/run/tracking.md 5-1). */
class AutoPauseDetector {
    private val window = ArrayDeque<RawLocation>()
    private var paused = false

    /** 필터 1~3번을 통과한 점을 넣는다. 지터로 버린 점도 정지 신호이므로 포함한다. */
    fun onPoint(point: RawLocation): AutoPauseTransition? {
        val last = window.lastOrNull()
        if (last != null && point.elapsedRealtimeMs - last.elapsedRealtimeMs > MAX_SAMPLE_GAP_MS) window.clear()
        window.addLast(point)
        trimWindow(point)
        val transition =
            when {
                !paused && isStopped(point) -> AutoPauseTransition.PAUSE
                paused && isMoving() -> AutoPauseTransition.RESUME
                else -> null
            }
        if (transition != null) {
            paused = transition == AutoPauseTransition.PAUSE
            window.clear()
            window.addLast(point)
        }
        return transition
    }

    fun reset() {
        window.clear()
        paused = false
    }

    private fun trimWindow(newest: RawLocation) {
        while (window.size >= 2 && newest.elapsedRealtimeMs - window[1].elapsedRealtimeMs >= STOP_WINDOW_MS) {
            window.removeFirst()
        }
    }

    private fun isStopped(newest: RawLocation): Boolean {
        val oldest = window.first()
        return newest.elapsedRealtimeMs - oldest.elapsedRealtimeMs >= STOP_WINDOW_MS &&
            newest.distanceTo(oldest) < STOP_DISTANCE_M
    }

    private fun isMoving(): Boolean {
        if (window.size <= RESUME_STEPS) return false
        val recent = window.takeLast(RESUME_STEPS + 1)
        return recent.zipWithNext { a, b -> b.speedFrom(a) >= RESUME_SPEED_MPS }.all { it } &&
            recent.first().distanceTo(recent.last()) >= RESUME_DISTANCE_M
    }

    private companion object {
        const val STOP_WINDOW_MS = 6_000L
        const val STOP_DISTANCE_M = 3.0
        const val MAX_SAMPLE_GAP_MS = 3_000L
        const val RESUME_STEPS = 2
        const val RESUME_SPEED_MPS = 1.2
        const val RESUME_DISTANCE_M = 4.0
    }
}
