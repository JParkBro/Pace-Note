package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RawLocation
import jpark.bro.pacenote.core.model.RunSplit
import org.junit.Assert.assertEquals
import org.junit.Test

class RunMetricsAccumulatorRestoreTest {
    /** 북쪽으로 1초에 3m씩, 정확도 5m */
    private fun north(
        meters: Double,
        elapsedMs: Long,
    ) = RawLocation(meters / METERS_PER_DEGREE, 0.0, null, 5f, null, elapsedMs, elapsedMs)

    private val restored =
        RunProgress(
            distanceM = 2_990.0,
            movingMs = 600_000L,
            nextSegment = 3,
            completedSplits = listOf(RunSplit(1, 1_000.0, 300_000L), RunSplit(2, 1_000.0, 290_000L)),
        )

    @Test
    fun `복구하면 저장된 거리와 운동 시간에서 이어감`() {
        val accumulator = RunMetricsAccumulator(startElapsedMs = 50_000L, autoPauseEnabled = false, restored = restored)

        val metrics = accumulator.metrics(nowElapsedMs = 60_000L)

        assertEquals(2_990.0, metrics.distanceM, 0.0)
        assertEquals(610_000L, metrics.movingMsAtEmit)
    }

    @Test
    fun `복구 후 첫 점은 저장된 다음 segment로 기록됨`() {
        val accumulator = RunMetricsAccumulator(startElapsedMs = 0L, autoPauseEnabled = false, restored = restored)

        assertEquals(3, accumulator.onLocation(north(0.0, 1_000L)).segment)
    }

    @Test
    fun `복구 후 3km를 넘으면 세 번째 구간은 복구 시점 이후 시간만 셈`() {
        val accumulator = RunMetricsAccumulator(startElapsedMs = 0L, autoPauseEnabled = false, restored = restored)

        // 복구 후 첫 점은 기준점이고, 10초 동안 30m를 더 달려 3,020m (3,000m는 복구 후 10m 지점)
        (0..10).forEach { accumulator.onLocation(north(it * 3.0, it * 1_000L)) }
        val splits = accumulator.completedSplits

        assertEquals(3, splits.size)
        // 2,000m 경계(590초)부터 3,000m(600초 + 복구 후 약 3.3초)까지
        assertEquals(13_333.0, splits[2].movingMs.toDouble(), 1.0)
    }

    @Test
    fun `복구 후 바로 마치면 저장된 구간과 남은 구간으로 끝남`() {
        val accumulator = RunMetricsAccumulator(startElapsedMs = 0L, autoPauseEnabled = false, restored = restored)

        val totals = accumulator.finish(nowElapsedMs = 0L)

        assertEquals(600_000L, totals.movingMs)
        assertEquals(listOf(1_000.0, 1_000.0, 990.0), totals.splits.map { it.distanceM })
        assertEquals(10_000L, totals.splits.last().movingMs)
    }

    private companion object {
        const val METERS_PER_DEGREE = 111_194.92664455873
    }
}
