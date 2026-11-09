package jpark.bro.pacenote.core.domain.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoPauseDetectorTest {
    private fun AutoPauseDetector.standUntilPaused() {
        (0..6).forEach { onPoint(pointAt(0.0, it * 1_000L)) }
    }

    @Test
    fun `6초 동안 3m 미만이면 일시정지`() {
        val detector = AutoPauseDetector()

        (0..5).forEach { assertNull(detector.onPoint(pointAt(it * 0.1, it * 1_000L))) }

        assertEquals(AutoPauseTransition.PAUSE, detector.onPoint(pointAt(0.6, 6_000)))
    }

    @Test
    fun `6초 동안 3m 이상 움직이면 일시정지하지 않음`() {
        val detector = AutoPauseDetector()

        (0..6).forEach { assertNull(detector.onPoint(pointAt(it * 0.5, it * 1_000L))) }
    }

    @Test
    fun `점 간격이 3초를 넘으면 정지로 보지 않음`() {
        val detector = AutoPauseDetector()

        detector.onPoint(pointAt(0.0, 0))

        assertNull(detector.onPoint(pointAt(0.0, 7_000)))
    }

    @Test
    fun `연속 2구간이 초속 1점2m 이상이고 4m 이상 움직이면 재개`() {
        val detector = AutoPauseDetector().apply { standUntilPaused() }

        assertNull(detector.onPoint(pointAt(2.0, 7_000)))

        assertEquals(AutoPauseTransition.RESUME, detector.onPoint(pointAt(4.0, 8_000)))
    }

    @Test
    fun `속도가 충분해도 4m 미만이면 재개하지 않음`() {
        val detector = AutoPauseDetector().apply { standUntilPaused() }

        assertNull(detector.onPoint(pointAt(1.5, 7_000)))
        assertNull(detector.onPoint(pointAt(3.0, 8_000)))
    }

    @Test
    fun `한 구간만 빠르면 재개하지 않음`() {
        val detector = AutoPauseDetector().apply { standUntilPaused() }

        assertNull(detector.onPoint(pointAt(5.0, 7_000)))
        assertNull(detector.onPoint(pointAt(5.1, 8_000)))
    }
}
