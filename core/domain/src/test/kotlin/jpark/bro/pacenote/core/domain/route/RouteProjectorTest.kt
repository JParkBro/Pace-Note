package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteProjectorTest {
    private val meter = 1 / 111_194.92664455873

    private fun at(
        northM: Double,
        eastM: Double,
        segment: Int = 0,
    ) = RoutePoint(northM * meter, eastM * meter, segment)

    @Test
    fun `빈 경로는 빈 결과`() {
        val route = RouteProjector.project(emptyList(), 1f, 0.1f)

        assertTrue(route.points.isEmpty())
        assertFalse(route.isDot)
    }

    @Test
    fun `범위가 30m 미만이면 중앙에 점 하나`() {
        val route = RouteProjector.project(listOf(at(0.0, 0.0), at(29.0, 0.0)), 1f, 0.1f)

        assertTrue(route.isDot)
        assertEquals(1, route.points.size)
        assertEquals(0.5f, route.points.single().x, 0f)
        assertEquals(0.5f, route.points.single().y, 0f)
    }

    @Test
    fun `범위가 30m면 선으로 표시`() {
        assertFalse(RouteProjector.project(listOf(at(0.0, 0.0), at(30.0, 0.0)), 1f, 0.1f).isDot)
    }

    @Test
    fun `북쪽이 위로 오고 패딩 안에 들어감`() {
        val route = RouteProjector.project(listOf(at(0.0, 0.0), at(100.0, 0.0)), 1f, 0.1f)

        assertEquals(0.9f, route.points[0].y, 0.001f)
        assertEquals(0.1f, route.points[1].y, 0.001f)
    }

    @Test
    fun `가로로 긴 영역에서도 실제 비율을 유지`() {
        val aspect = 2f
        val route = RouteProjector.project(listOf(at(0.0, 0.0), at(100.0, 100.0)), aspect, 0.1f)

        val width = (route.points[1].x - route.points[0].x) * aspect
        val height = route.points[0].y - route.points[1].y
        assertEquals(1f, width / height, 0.001f)
    }

    @Test
    fun `남는 쪽은 가운데 정렬`() {
        val route = RouteProjector.project(listOf(at(0.0, 0.0), at(100.0, 0.0)), 2f, 0.1f)

        assertEquals(0.5f, route.points[0].x, 0.001f)
    }

    @Test
    fun `segment를 유지`() {
        val route = RouteProjector.project(listOf(at(0.0, 0.0), at(100.0, 0.0, segment = 1)), 1f, 0.1f)

        assertEquals(listOf(0, 1), route.points.map { it.segment })
    }
}
