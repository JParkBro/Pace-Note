package jpark.bro.pacenote.widget

import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.model.WeeklySummary
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeGoalRepository
import jpark.bro.pacenote.core.testing.fake.FakeSettingsRepository
import jpark.bro.pacenote.core.testing.fake.FakeStatsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WeeklyWidgetDataTest {
    private val thisWeek = LocalDate.of(2026, 10, 26)
    private val summary = WeeklySummary(thisWeek, 12_400.0, 4_360_000, 3)

    @Test
    fun `목표가 있으면 진행률을 WeeklyProgress로 구함`() {
        val goal = Goal("g", null, GoalType.DISTANCE, 15_000.0, thisWeek, 0, 0, null)

        val data = WeeklyWidgetData.of(summary, goal, DistanceUnit.KM, AppThemeType.VOLT)

        assertEquals(0.8267, data.ratio, 0.0001)
        assertEquals(15_000.0, data.goalValue!!, 0.0)
    }

    @Test
    fun `목표가 없으면 거리만`() {
        val data = WeeklyWidgetData.of(summary, null, DistanceUnit.KM, AppThemeType.VOLT)

        assertNull(data.goalType)
        assertEquals(0.0, data.ratio, 0.0)
        assertEquals(12_400.0, data.distanceM, 0.0)
    }

    @Test
    fun `이번 주 값과 앱 테마 · 단위를 Repository에서 읽음`() =
        runTest {
            val stats = FakeStatsRepository().apply { weeklySummaries.value = mapOf(thisWeek to summary) }
            val settings =
                FakeSettingsRepository().apply { savedTheme.value = ThemeSetting(AppThemeType.RIVER) }

            val data = WidgetDataSource(stats, FakeGoalRepository(), settings, FakeClock()).load()

            assertEquals(12_400.0, data.distanceM, 0.0)
            assertEquals(AppThemeType.RIVER, data.themeType)
        }

    private fun seoul(dateTime: String) = LocalDateTime.parse(dateTime).atZone(ZoneId.of("Asia/Seoul"))

    @Test
    fun `일요일 23시 59분이면 1분 뒤 다시 그림`() {
        assertEquals(Duration.ofMinutes(1), WidgetRefreshWorker.delayToNextWeek(seoul("2026-11-01T23:59")))
    }

    @Test
    fun `월요일 0시 정각이면 일주일 뒤`() {
        assertEquals(Duration.ofDays(7), WidgetRefreshWorker.delayToNextWeek(seoul("2026-11-02T00:00")))
    }

    @Test
    fun `시간대가 바뀌면 그 시간대의 월요일 0시 기준`() {
        val newYork: ZonedDateTime = LocalDateTime.parse("2026-10-29T09:00").atZone(ZoneId.of("America/New_York"))

        val next = newYork.plus(WidgetRefreshWorker.delayToNextWeek(newYork))

        assertEquals(LocalDateTime.parse("2026-11-02T00:00"), next.toLocalDateTime())
    }
}
