package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RunSplit
import org.junit.Assert.assertEquals
import org.junit.Test

class SplitCalculatorTest {
    @Test
    fun `1000m를 정확히 통과하면 구간 하나`() {
        val calculator = SplitCalculator().apply { add(990.0, 1_000.0, 297_000, 300_000) }

        assertEquals(listOf(RunSplit(1, 1_000.0, 300_000)), calculator.finish(1_000.0, 300_000))
    }

    @Test
    fun `999m는 구간을 만들지 않음`() {
        val calculator = SplitCalculator().apply { add(0.0, 999.0, 0, 300_000) }

        assertEquals(listOf(RunSplit(1, 999.0, 300_000)), calculator.finish(999.0, 300_000))
    }

    @Test
    fun `경계는 두 점 사이를 선형 보간`() {
        val calculator = SplitCalculator().apply { add(900.0, 1_100.0, 100_000, 300_000) }

        assertEquals(RunSplit(1, 1_000.0, 200_000), calculator.finish(1_100.0, 300_000).first())
    }

    @Test
    fun `한 번에 여러 km를 넘으면 구간을 모두 만듦`() {
        val calculator = SplitCalculator().apply { add(0.0, 2_500.0, 0, 750_000) }

        assertEquals(
            listOf(
                RunSplit(1, 1_000.0, 300_000),
                RunSplit(2, 1_000.0, 300_000),
                RunSplit(3, 500.0, 150_000),
            ),
            calculator.finish(2_500.0, 750_000),
        )
    }

    @Test
    fun `남은 거리가 10m 미만이면 표에 넣지 않음`() {
        val calculator = SplitCalculator().apply { add(0.0, 1_000.0, 0, 300_000) }

        assertEquals(1, calculator.finish(1_009.9, 303_000).size)
    }

    @Test
    fun `남은 거리가 10m면 표에 넣음`() {
        val calculator = SplitCalculator().apply { add(0.0, 1_000.0, 0, 300_000) }

        assertEquals(RunSplit(2, 10.0, 3_000), calculator.finish(1_010.0, 303_000).last())
    }
}
