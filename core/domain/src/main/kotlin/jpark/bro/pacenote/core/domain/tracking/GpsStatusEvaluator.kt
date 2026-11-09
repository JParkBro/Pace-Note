package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.RawLocation

/**
 * GPS 신호 상태 (tracking.md 4-1). 준비(P1-c)의 "시작" 활성 조건과 기록 중 신호 표시에 쓴다.
 *
 * - 마지막 점에서 10초 넘게 지났거나 점이 아직 없으면 [GpsStatus.LOST]
 * - 최근 3개 점 중 정확도 20m 이하가 2개 이상이면 [GpsStatus.GOOD]
 * - 그 외(대략 위치만 허용한 경우 포함)는 [GpsStatus.WEAK]
 */
class GpsStatusEvaluator {
    private val recentAccuracyM = ArrayDeque<Float>(WINDOW)
    private var lastPointElapsedMs: Long? = null

    fun onPoint(point: RawLocation) {
        if (recentAccuracyM.size == WINDOW) recentAccuracyM.removeFirst()
        recentAccuracyM.addLast(point.accuracyM)
        lastPointElapsedMs = point.elapsedRealtimeMs
    }

    fun status(nowElapsedMs: Long): GpsStatus {
        val last = lastPointElapsedMs
        return when {
            last == null || nowElapsedMs - last > LOST_AFTER_MS -> GpsStatus.LOST
            recentAccuracyM.count { it <= GOOD_ACCURACY_M } >= GOOD_MIN_COUNT -> GpsStatus.GOOD
            else -> GpsStatus.WEAK
        }
    }

    private companion object {
        const val WINDOW = 3
        const val GOOD_MIN_COUNT = 2
        const val GOOD_ACCURACY_M = 20f
        const val LOST_AFTER_MS = 10_000L
    }
}
