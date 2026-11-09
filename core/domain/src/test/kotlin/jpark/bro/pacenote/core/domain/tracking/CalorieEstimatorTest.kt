package jpark.bro.pacenote.core.domain.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalorieEstimatorTest {
    @Test
    fun `체중 × 거리 × 1점036을 반올림`() {
        assertEquals(351, CalorieEstimator.estimate(65.0, 5_210.0))
    }

    @Test
    fun `체중이 없으면 null`() {
        assertNull(CalorieEstimator.estimate(null, 5_210.0))
    }
}
