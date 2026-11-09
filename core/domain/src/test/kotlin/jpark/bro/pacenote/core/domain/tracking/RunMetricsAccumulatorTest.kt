package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.PauseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunMetricsAccumulatorTest {
    private fun accumulator(autoPause: Boolean = false) = RunMetricsAccumulator(0, autoPause)

    @Test
    fun `채택된 점 사이 거리를 더함`() {
        val run = accumulator()

        (0..10).forEach { run.onLocation(pointAt(it * 4.0, it * 1_000L)) }

        assertEquals(40.0, run.finish(10_000).distanceM, 0.01)
    }

    @Test
    fun `정확도가 나쁜 점은 거리에 넣지 않음`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(10.0, 1_000, accuracyM = 30f))
        run.onLocation(pointAt(4.0, 2_000))

        assertEquals(4.0, run.finish(2_000).distanceM, 0.01)
    }

    @Test
    fun `튄 점 다음의 정상 점은 채택`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(500.0, 1_000))

        assertEquals(true, run.onLocation(pointAt(8.0, 2_000)).accepted)
    }

    @Test
    fun `일시정지 중 이동한 거리와 시간은 넣지 않음`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(10.0, 3_000))
        run.pause(3_000)
        run.onLocation(pointAt(100.0, 33_000))
        run.resume(33_000)
        run.onLocation(pointAt(100.0, 33_000 + 1))
        run.onLocation(pointAt(110.0, 36_000))

        val totals = run.finish(36_000)
        assertEquals(20.0, totals.distanceM, 0.01)
        assertEquals(6_000, totals.movingMs)
        assertEquals(2, totals.segmentCount)
    }

    @Test
    fun `30초 이하 공백은 직선으로 이어 거리에 포함`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(90.0, 30_000))

        val totals = run.finish(30_000)
        assertEquals(90.0, totals.distanceM, 0.01)
        assertEquals(1, totals.segmentCount)
    }

    @Test
    fun `30초 넘는 공백은 새 구간이고 거리는 넣지 않지만 시간은 계속`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(90.0, 30_001))

        val totals = run.finish(30_001)
        assertEquals(0.0, totals.distanceM, 0.01)
        assertEquals(30_001, totals.movingMs)
        assertEquals(2, totals.segmentCount)
    }

    @Test
    fun `자동 일시정지 시간은 운동 시간에서 빼고 재개 때 멈춘 지점부터 거리를 넣음`() {
        val run = accumulator(autoPause = true)

        (0..6).forEach { run.onLocation(pointAt(0.0, it * 1_000L)) }
        assertEquals(PauseState.AUTO_PAUSED, run.onLocation(pointAt(0.0, 16_000)).pauseState)
        run.onLocation(pointAt(3.0, 17_000))
        val resumed = run.onLocation(pointAt(6.0, 18_000))

        assertEquals(PauseState.RUNNING, resumed.pauseState)
        val totals = run.finish(18_000)
        assertEquals(6.0, totals.distanceM, 0.01)
        assertEquals(6_000, totals.movingMs)
    }

    @Test
    fun `자동 일시정지를 끄면 멈춰도 시간이 계속`() {
        val run = accumulator(autoPause = false)

        (0..10).forEach { assertEquals(PauseState.RUNNING, run.onLocation(pointAt(0.0, it * 1_000L)).pauseState) }

        assertEquals(10_000, run.finish(10_000).movingMs)
    }

    @Test
    fun `직접 일시정지하면 움직여도 자동 재개하지 않음`() {
        val run = accumulator(autoPause = true)

        run.onLocation(pointAt(0.0, 0))
        run.pause(1_000)

        (2..6).forEach {
            assertEquals(PauseState.USER_PAUSED, run.onLocation(pointAt(it * 5.0, it * 1_000L)).pauseState)
        }
    }

    @Test
    fun `거리가 50m 미만이면 평균 페이스는 null`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(49.0, 10_000))

        assertNull(run.metrics(10_000).avgPaceSecPerKm)
    }

    @Test
    fun `평균 페이스는 운동 시간을 거리로 나눔`() {
        val run = accumulator()

        run.onLocation(pointAt(0.0, 0))
        run.onLocation(pointAt(100.0, 25_000))

        assertEquals(250.0, requireNotNull(run.metrics(25_000).avgPaceSecPerKm), 0.5)
    }
}
