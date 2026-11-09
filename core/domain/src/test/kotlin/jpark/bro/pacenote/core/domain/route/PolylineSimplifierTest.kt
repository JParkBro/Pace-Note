package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.domain.tracking.GpxFixtures
import jpark.bro.pacenote.core.domain.tracking.RunMetricsAccumulator
import jpark.bro.pacenote.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineSimplifierTest {
    private val meter = 1 / 111_194.92664455873

    @Test
    fun `허용 오차 안의 점은 지움`() {
        val points =
            listOf(RoutePoint(0.0, 0.0, 0), RoutePoint(100 * meter, 4.9 * meter, 0), RoutePoint(200 * meter, 0.0, 0))

        assertEquals(2, PolylineSimplifier.simplify(points).points.size)
    }

    @Test
    fun `허용 오차를 넘는 점은 남김`() {
        val points =
            listOf(RoutePoint(0.0, 0.0, 0), RoutePoint(100 * meter, 5.1 * meter, 0), RoutePoint(200 * meter, 0.0, 0))

        assertEquals(3, PolylineSimplifier.simplify(points).points.size)
    }

    @Test
    fun `segment별로 나눠 점 개수를 돌려줌`() {
        val points =
            (0..2).map { RoutePoint(it * meter * 100, 0.0, 0) } + (5..7).map { RoutePoint(it * meter * 100, 0.0, 3) }

        val route = PolylineSimplifier.simplify(points)

        assertEquals(listOf(2, 2), route.segmentCounts)
    }

    @Test
    fun `점 하나짜리 segment도 유지`() {
        assertEquals(listOf(1), PolylineSimplifier.simplify(listOf(RoutePoint(0.0, 0.0, 0))).segmentCounts)
    }

    @Test
    fun `지그재그 경로도 500점 이하`() {
        val points = (0..3_000).map { RoutePoint(it * 3 * meter, (if (it % 2 == 0) 0 else 40) * meter, 0) }

        assertTrue(PolylineSimplifier.simplify(points).points.size <= 500)
    }

    @Test
    fun `60분 러닝은 500점 이하이고 인코딩이 20000자 이하`() {
        val run = RunMetricsAccumulator(startElapsedMs = 0, autoPauseEnabled = true)
        val accepted =
            GpxFixtures.points("long_60min").mapNotNull { point ->
                val outcome = run.onLocation(point)
                RoutePoint(point.lat, point.lng, outcome.segment).takeIf { outcome.accepted }
            }

        val route = PolylineSimplifier.simplify(accepted)

        assertTrue(route.points.size <= 500)
        assertTrue(PolylineCodec.encode(route.points).length <= 20_000)
    }
}
