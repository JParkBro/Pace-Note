package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.PauseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GpxReplayTest {
    private fun replay(name: String) {
        val points = GpxFixtures.points(name)
        val expected = GpxFixtures.expected(name)
        val run = RunMetricsAccumulator(startElapsedMs = 0, autoPauseEnabled = true)

        var autoPauses = 0
        var previous = PauseState.RUNNING
        points.forEach { point ->
            val state = run.onLocation(point).pauseState
            if (state == PauseState.AUTO_PAUSED && previous != PauseState.AUTO_PAUSED) autoPauses++
            previous = state
        }
        val totals = run.finish(points.last().elapsedRealtimeMs)

        assertEquals("autoPauseCount", expected.autoPauseCount, autoPauses)
        assertEquals("segmentCount", expected.segmentCount, totals.segmentCount)
        expected.distanceM?.let { distance ->
            val tolerance = distance * requireNotNull(expected.distanceTolerancePct) / 100
            assertEquals("distanceM", distance, totals.distanceM, tolerance)
        }
        expected.maxDistanceM?.let { assertTrue("distanceM=${totals.distanceM}", totals.distanceM < it) }
        expected.movingMs?.let { moving ->
            assertTrue(
                "movingMs=${totals.movingMs}",
                abs(moving - totals.movingMs) <= requireNotNull(expected.movingToleranceMs),
            )
        }
        expected.splitCount?.let { assertEquals("splitCount", it, totals.splits.size) }
        expected.splitMovingMs?.let { moving ->
            totals.splits.forEach { split ->
                assertTrue("split=$split", abs(moving - split.movingMs) <= requireNotNull(expected.splitToleranceMs))
            }
        }
    }

    @Test
    fun `직선 5km는 거리 오차 1퍼센트 안이고 구간 5개가 각 5분`() = replay("straight_5km")

    @Test
    fun `튄 점이 섞인 3km 루프는 거리 오차 2퍼센트 안`() = replay("loop_with_spikes")

    @Test
    fun `제자리 흔들림은 거리가 10m 미만이고 자동 일시정지에 들어감`() = replay("standing_jitter")

    @Test
    fun `신호등 3번은 자동 일시정지 3번이고 정지 시간을 뺌`() = replay("traffic_light")

    @Test
    fun `20초 공백은 잇고 60초 공백은 새 segment`() = replay("tunnel_gap")

    @Test
    fun `60분 러닝도 거리 오차 1퍼센트 안`() = replay("long_60min")
}
