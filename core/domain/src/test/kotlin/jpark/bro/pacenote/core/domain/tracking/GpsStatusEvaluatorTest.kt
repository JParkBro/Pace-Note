package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.RawLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class GpsStatusEvaluatorTest {
    private fun point(
        accuracyM: Float,
        elapsedMs: Long,
    ) = RawLocation(0.0, 0.0, null, accuracyM, null, elapsedMs, elapsedMs)

    private fun evaluatorWith(vararg accuracies: Float) =
        GpsStatusEvaluator().apply {
            accuracies.forEachIndexed { i, accuracy -> onPoint(point(accuracy, i * 1_000L)) }
        }

    @Test
    fun `점이 없으면 LOST`() {
        assertEquals(GpsStatus.LOST, GpsStatusEvaluator().status(0))
    }

    @Test
    fun `최근 3개 중 20m 이하가 2개면 GOOD`() {
        assertEquals(GpsStatus.GOOD, evaluatorWith(30f, 20f, 20f).status(2_000))
    }

    @Test
    fun `최근 3개 중 20m 이하가 1개면 WEAK`() {
        assertEquals(GpsStatus.WEAK, evaluatorWith(20f, 20.1f, 40f).status(2_000))
    }

    @Test
    fun `오래된 정확한 점은 최근 3개에서 빠짐`() {
        assertEquals(GpsStatus.WEAK, evaluatorWith(5f, 5f, 40f, 40f).status(3_000))
    }

    @Test
    fun `대략 위치(수백 m)만 오면 WEAK`() {
        assertEquals(GpsStatus.WEAK, evaluatorWith(500f, 500f, 500f).status(2_000))
    }

    @Test
    fun `마지막 점에서 10초는 유지, 10초를 넘으면 LOST`() {
        val evaluator = evaluatorWith(5f, 5f)

        assertEquals(GpsStatus.GOOD, evaluator.status(11_000))
        assertEquals(GpsStatus.LOST, evaluator.status(11_001))
    }
}
