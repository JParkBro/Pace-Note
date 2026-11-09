package jpark.bro.pacenote.core.location

import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock

/**
 * GPX 트랙을 원래 간격대로 다시 보낸다 (테스트 · 디버그 빌드의 실내 확인용, tracking.md 8-3).
 * 시각은 재생하는 순간의 값으로 바꿔서 보내므로, 받는 쪽은 실제 위치와 구별하지 않는다.
 *
 * @param speed 1.0이면 실제 속도, 10.0이면 10배 빠르게
 */
class GpxReplaySource(
    private val points: List<GpxPoint>,
    private val elapsedClock: ElapsedClock,
    private val clock: Clock,
    private val speed: Double = 1.0,
) : LocationSource {
    init {
        require(speed > 0) { "재생 속도는 0보다 커야 한다" }
    }

    override fun locations(): Flow<RawLocation> =
        flow {
            var previousTimeMs: Long? = null
            for (point in points) {
                previousTimeMs?.let { delay(((point.timeMs - it) / speed).toLong().coerceAtLeast(0)) }
                previousTimeMs = point.timeMs
                emit(point.toRawLocation())
            }
        }

    override suspend fun lastKnown(): RawLocation? = points.firstOrNull()?.toRawLocation()

    override fun isLocationEnabled(): Boolean = true

    private fun GpxPoint.toRawLocation() =
        RawLocation(
            lat = lat,
            lng = lng,
            altitude = altitude,
            accuracyM = accuracyM ?: DEFAULT_ACCURACY_M,
            speedMps = null,
            recordedAt = clock.millis(),
            elapsedRealtimeMs = elapsedClock.elapsedRealtimeMs(),
        )

    private companion object {
        /** 정확도가 없는 일반 GPX는 필터를 통과하는 좋은 신호로 본다 */
        const val DEFAULT_ACCURACY_M = 5f
    }
}
