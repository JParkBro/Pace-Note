package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.model.RoutePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class PolylineCodecTest {
    private val sample =
        listOf(RoutePoint(38.5, -120.2, 0), RoutePoint(40.7, -120.95, 0), RoutePoint(43.252, -126.453, 0))

    @Test
    fun `Google 공식 예제와 같게 인코딩`() {
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", PolylineCodec.encode(sample))
    }

    @Test
    fun `Google 공식 예제를 디코딩`() {
        val decoded = PolylineCodec.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@", listOf(3))

        decoded.zip(sample).forEach { (actual, expected) ->
            assertEquals(expected.lat, actual.lat, 1e-5)
            assertEquals(expected.lng, actual.lng, 1e-5)
        }
    }

    @Test
    fun `왕복해도 오차가 정밀도 안이고 segment를 복원`() {
        val points = listOf(RoutePoint(0.123456, -0.654321, 0), RoutePoint(0.0, 0.0, 1), RoutePoint(-1.5, 2.25, 1))

        val decoded = PolylineCodec.decode(PolylineCodec.encode(points), listOf(1, 2))

        decoded.zip(points).forEach { (actual, expected) ->
            assertEquals(expected.lat, actual.lat, 1e-5)
            assertEquals(expected.lng, actual.lng, 1e-5)
        }
        assertEquals(listOf(0, 1, 1), decoded.map { it.segment })
    }

    @Test
    fun `빈 경로는 빈 문자열`() {
        assertEquals("", PolylineCodec.encode(emptyList()))
        assertEquals(emptyList<RoutePoint>(), PolylineCodec.decode("", emptyList()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `점 개수가 맞지 않으면 예외`() {
        PolylineCodec.decode(PolylineCodec.encode(sample), listOf(2))
    }
}
