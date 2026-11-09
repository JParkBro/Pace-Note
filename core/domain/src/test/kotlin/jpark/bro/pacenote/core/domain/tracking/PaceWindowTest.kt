package jpark.bro.pacenote.core.domain.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaceWindowTest {
    @Test
    fun `데이터가 없으면 null`() {
        assertNull(PaceWindow().currentPaceSecPerKm())
    }

    @Test
    fun `창이 덜 차면 null`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(199.0, 29_999)
            }

        assertNull(window.currentPaceSecPerKm())
    }

    @Test
    fun `200m가 먼저 차면 200m 구간 평균`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(200.0, 20_000)
            }

        assertEquals(100.0, requireNotNull(window.currentPaceSecPerKm()), 0.001)
    }

    @Test
    fun `30초가 먼저 차면 30초 구간 평균`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(90.0, 30_000)
            }

        assertEquals(333.333, requireNotNull(window.currentPaceSecPerKm()), 0.001)
    }

    @Test
    fun `오래된 표본은 창에서 빠짐`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(30.0, 30_000)
                add(120.0, 60_000)
            }

        assertEquals(333.333, requireNotNull(window.currentPaceSecPerKm()), 0.001)
    }

    @Test
    fun `정확히 20분 페이스는 표시`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(25.0, 30_000)
            }

        assertEquals(1_200.0, requireNotNull(window.currentPaceSecPerKm()), 0.001)
    }

    @Test
    fun `20분 페이스보다 느리면 null`() {
        val window =
            PaceWindow().apply {
                add(0.0, 0)
                add(24.9, 30_000)
            }

        assertNull(window.currentPaceSecPerKm())
    }

    @Test
    fun `움직이지 않으면 null`() {
        val window =
            PaceWindow().apply {
                add(10.0, 0)
                add(10.0, 30_000)
            }

        assertNull(window.currentPaceSecPerKm())
    }
}
