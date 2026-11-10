package jpark.bro.pacenote.core.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

class RunTitleTest {
    private fun at(dateTime: String) =
        RunTitle.defaultOf(
            LocalDateTime
                .parse(dateTime)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli(),
            "Asia/Seoul",
        )

    @Test
    fun `수요일 오전 6시 40분은 수요일 아침`() {
        assertEquals(DefaultRunTitle(DayOfWeek.WEDNESDAY, TimeOfDay.MORNING), at("2026-10-28T06:40"))
    }

    @Test
    fun `경계 5시 · 11시 · 17시 · 21시`() {
        assertEquals(TimeOfDay.NIGHT, at("2026-10-28T04:59").timeOfDay)
        assertEquals(TimeOfDay.MORNING, at("2026-10-28T05:00").timeOfDay)
        assertEquals(TimeOfDay.MORNING, at("2026-10-28T10:59").timeOfDay)
        assertEquals(TimeOfDay.MIDDAY, at("2026-10-28T11:00").timeOfDay)
        assertEquals(TimeOfDay.MIDDAY, at("2026-10-28T16:59").timeOfDay)
        assertEquals(TimeOfDay.EVENING, at("2026-10-28T17:00").timeOfDay)
        assertEquals(TimeOfDay.EVENING, at("2026-10-28T20:59").timeOfDay)
        assertEquals(TimeOfDay.NIGHT, at("2026-10-28T21:00").timeOfDay)
    }

    @Test
    fun `자정을 넘긴 밤은 그 날짜의 요일`() {
        assertEquals(DefaultRunTitle(DayOfWeek.THURSDAY, TimeOfDay.NIGHT), at("2026-10-29T00:30"))
    }
}
