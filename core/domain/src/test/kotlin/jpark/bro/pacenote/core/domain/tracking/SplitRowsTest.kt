package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RunSplit
import org.junit.Assert.assertEquals
import org.junit.Test

class SplitRowsTest {
    @Test
    fun `순서대로 누적 거리를 쌓고 마지막 짧은 구간도 환산 페이스`() {
        val rows =
            SplitRows.of(
                listOf(RunSplit(2, 210.0, 79_000), RunSplit(0, 1_000.0, 352_000), RunSplit(1, 1_000.0, 344_000)),
            )

        assertEquals(listOf(1_000.0, 2_000.0, 2_210.0), rows.map { it.endDistanceM })
        assertEquals(352.0, rows.first().paceSecPerKm!!, 0.0001)
        assertEquals(376.19, rows.last().paceSecPerKm!!, 0.01)
    }

    @Test
    fun `구간이 없으면 빈 표`() {
        assertEquals(emptyList<SplitRow>(), SplitRows.of(emptyList()))
    }
}
