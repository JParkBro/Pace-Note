package jpark.bro.pacenote.core.domain.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaceTest {
    @Test
    fun `5점21km를 30분12초에 달리면 km당 약 347초`() {
        assertEquals(347.79, Pace.secPerKm(1_812_000, 5_210.0)!!, 0.01)
    }

    @Test
    fun `50m는 페이스를 계산함`() {
        assertEquals(400.0, Pace.secPerKm(20_000, 50.0)!!, 0.0001)
    }

    @Test
    fun `50m 미만이면 null`() {
        assertNull(Pace.secPerKm(20_000, 49.9))
    }

    @Test
    fun `거리 0이면 null`() {
        assertNull(Pace.secPerKm(0, 0.0))
    }

    @Test
    fun `최소 거리를 낮추면 짧은 마지막 구간도 환산 페이스를 구함`() {
        assertEquals(360.0, Pace.secPerKm(7_200, 20.0, minDistanceM = 10.0)!!, 0.0001)
    }

    @Test
    fun `최소 거리를 0으로 낮춰도 거리 0이면 null`() {
        assertNull(Pace.secPerKm(1_000, 0.0, minDistanceM = 0.0))
    }
}
