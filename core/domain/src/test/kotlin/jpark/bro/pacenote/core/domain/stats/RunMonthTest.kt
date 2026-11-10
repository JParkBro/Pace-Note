package jpark.bro.pacenote.core.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class RunMonthTest {
    private fun ms(
        dateTime: String,
        zone: String,
    ) = LocalDateTime
        .parse(dateTime)
        .atZone(ZoneId.of(zone))
        .toInstant()
        .toEpochMilli()

    @Test
    fun `10월 31일 23시 59분 기록은 10월`() {
        assertEquals(YearMonth.of(2026, 10), RunMonth.of(ms("2026-10-31T23:59", "Asia/Seoul"), "Asia/Seoul"))
    }

    @Test
    fun `11월 1일 0시 기록은 11월`() {
        assertEquals(YearMonth.of(2026, 11), RunMonth.of(ms("2026-11-01T00:00", "Asia/Seoul"), "Asia/Seoul"))
    }

    @Test
    fun `기록의 시간대 기준이라 UTC로는 전달이어도 그 기록의 달`() {
        // 서울 11.1 오전 6시 = UTC 10.31 21시
        assertEquals(YearMonth.of(2026, 11), RunMonth.of(ms("2026-11-01T06:00", "Asia/Seoul"), "Asia/Seoul"))
    }
}
