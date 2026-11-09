package jpark.bro.pacenote.core.domain.tracking

import org.junit.Assert.assertEquals
import org.junit.Test

class PointFilterTest {
    private val anchor = pointAt(0.0, 0)

    private fun evaluate(
        northM: Double,
        elapsedMs: Long = 1_000,
        accuracyM: Float = 5f,
        previousElapsedMs: Long? = 0,
        receivedAtElapsedMs: Long = elapsedMs,
    ) = PointFilter.evaluate(pointAt(northM, elapsedMs, accuracyM), previousElapsedMs, anchor, receivedAtElapsedMs)

    @Test
    fun `정확도 25m는 채택`() {
        assertEquals(PointDecision.ACCEPTED, evaluate(northM = 12.6, accuracyM = 25f, elapsedMs = 2_000))
    }

    @Test
    fun `정확도 25점1m를 넘는 점은 버림`() {
        assertEquals(PointDecision.REJECTED_ACCURACY, evaluate(northM = 5.0, accuracyM = 25.1f))
    }

    @Test
    fun `이전 점과 시각이 같으면 버림`() {
        assertEquals(PointDecision.REJECTED_STALE, evaluate(northM = 5.0, elapsedMs = 1_000, previousElapsedMs = 1_000))
    }

    @Test
    fun `이전 점보다 이른 점은 버림`() {
        assertEquals(PointDecision.REJECTED_STALE, evaluate(northM = 5.0, elapsedMs = 900, previousElapsedMs = 1_000))
    }

    @Test
    fun `받은 시각보다 10초 넘게 오래된 점은 버림`() {
        assertEquals(PointDecision.REJECTED_STALE, evaluate(northM = 5.0, receivedAtElapsedMs = 11_001))
    }

    @Test
    fun `받은 시각보다 정확히 10초 오래된 점은 채택`() {
        assertEquals(PointDecision.ACCEPTED, evaluate(northM = 5.0, receivedAtElapsedMs = 11_000))
    }

    @Test
    fun `채택 점 대비 초속 12m를 넘으면 버림`() {
        assertEquals(PointDecision.REJECTED_SPEED, evaluate(northM = 12.1))
    }

    @Test
    fun `채택 점 대비 초속 12m 이하는 채택`() {
        assertEquals(PointDecision.ACCEPTED, evaluate(northM = 11.9))
    }

    @Test
    fun `3m 미만 이동은 지터로 버림`() {
        assertEquals(PointDecision.REJECTED_JITTER, evaluate(northM = 2.9))
    }

    @Test
    fun `정확도가 낮으면 정확도 절반 미만 이동도 지터로 버림`() {
        assertEquals(PointDecision.REJECTED_JITTER, evaluate(northM = 9.9, accuracyM = 20f, elapsedMs = 2_000))
    }

    @Test
    fun `첫 점은 비교 대상이 없어 채택`() {
        val decision = PointFilter.evaluate(pointAt(0.0, 0), null, null, 0)

        assertEquals(PointDecision.ACCEPTED, decision)
    }

    @Test
    fun `지터로 버린 점은 정지 판단에 쓸 수 있음`() {
        assertEquals(true, PointDecision.REJECTED_JITTER.isUsable)
        assertEquals(false, PointDecision.REJECTED_SPEED.isUsable)
    }
}
