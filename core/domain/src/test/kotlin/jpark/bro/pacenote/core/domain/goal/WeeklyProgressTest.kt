package jpark.bro.pacenote.core.domain.goal

import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunStatus
import jpark.bro.pacenote.core.model.WeeklySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class WeeklyProgressTest {
    private val monday = LocalDate.of(2026, 10, 26)
    private val seoul = "Asia/Seoul"

    private fun run(
        start: ZonedDateTime,
        distanceM: Double = 1_000.0,
        movingMs: Long = 300_000,
        status: RunStatus = RunStatus.FINISHED,
        deletedAt: Long? = null,
    ) = Run(
        id = start.toString(),
        ownerUserId = null,
        status = status,
        startedAt = start.toInstant().toEpochMilli(),
        endedAt = null,
        zoneId = start.zone.id,
        distanceM = distanceM,
        movingMs = movingMs,
        elapsedMs = movingMs,
        calories = null,
        title = null,
        memo = null,
        source = RunSource.GPS,
        activityType = ActivityType.OUTDOOR_RUN,
        planId = null,
        routePolyline = null,
        routeSegments = emptyList(),
        splits = emptyList(),
        schemaVersion = 1,
        createdAt = 0,
        updatedAt = 0,
        deletedAt = deletedAt,
    )

    private fun inSeoul(
        day: LocalDate,
        hour: Int,
        minute: Int = 0,
    ) = day.atTime(hour, minute).atZone(ZoneId.of(seoul))

    private fun goal(
        weekStart: LocalDate,
        value: Double,
        type: GoalType = GoalType.DISTANCE,
        updatedAt: Long = 0,
        deletedAt: Long? = null,
    ) = Goal(weekStart.toString() + updatedAt, null, type, value, weekStart, 0, updatedAt, deletedAt)

    @Test
    fun `월요일은 그 날이 주 시작`() {
        assertEquals(monday, WeeklyProgress.weekStart(monday))
    }

    @Test
    fun `일요일은 그 주 월요일이 주 시작`() {
        assertEquals(monday, WeeklyProgress.weekStart(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun `일요일 23시59분은 이번 주, 월요일 0시는 다음 주`() {
        val sunday = LocalDate.of(2026, 11, 1)

        assertEquals(monday, WeeklyProgress.weekStartOf(run(inSeoul(sunday, 23, 59))))
        assertEquals(monday.plusWeeks(1), WeeklyProgress.weekStartOf(run(inSeoul(monday.plusWeeks(1), 0))))
    }

    @Test
    fun `주 경계는 기록의 시간대로 판단`() {
        val newYork = monday.atTime(23, 0).atZone(ZoneId.of("America/New_York"))

        assertEquals(monday, WeeklyProgress.weekStartOf(run(newYork)))
    }

    @Test
    fun `이번 주의 끝난 기록만 합산`() {
        val runs =
            listOf(
                run(inSeoul(monday, 7), distanceM = 3_980.0, movingMs = 1_445_000),
                run(inSeoul(monday.plusDays(1), 20), distanceM = 3_210.0, movingMs = 1_103_000),
                run(inSeoul(monday.plusDays(2), 6), status = RunStatus.ACTIVE),
                run(inSeoul(monday.plusDays(2), 7), status = RunStatus.DISCARDED),
                run(inSeoul(monday.plusDays(3), 7), deletedAt = 1),
                run(inSeoul(monday.minusDays(1), 7)),
            )

        assertEquals(WeeklySummary(monday, 7_190.0, 2_548_000, 2), WeeklyProgress.summarize(runs, monday))
    }

    @Test
    fun `목표 변경은 적용 시작 주부터 반영`() {
        val goals = listOf(goal(monday.minusWeeks(2), 10_000.0), goal(monday, 15_000.0))

        assertEquals(10_000.0, requireNotNull(WeeklyProgress.goalFor(goals, monday.minusWeeks(1))).value, 0.0)
        assertEquals(15_000.0, requireNotNull(WeeklyProgress.goalFor(goals, monday)).value, 0.0)
    }

    @Test
    fun `같은 주에 여러 번 바꾸면 마지막 값`() {
        val goals = listOf(goal(monday, 10_000.0, updatedAt = 1), goal(monday, 15_000.0, updatedAt = 2))

        assertEquals(15_000.0, requireNotNull(WeeklyProgress.goalFor(goals, monday)).value, 0.0)
    }

    @Test
    fun `적용 전이거나 지운 목표는 없음`() {
        assertNull(WeeklyProgress.goalFor(listOf(goal(monday.plusWeeks(1), 1.0)), monday))
        assertNull(WeeklyProgress.goalFor(listOf(goal(monday, 1.0, deletedAt = 1)), monday))
    }

    @Test
    fun `거리 목표 진행률`() {
        val summary = WeeklySummary(monday, 12_400.0, 0, 3)

        assertEquals(0.8267, WeeklyProgress.ratio(summary, goal(monday, 15_000.0)), 0.0001)
    }

    @Test
    fun `횟수 목표 진행률`() {
        val summary = WeeklySummary(monday, 12_400.0, 0, 3)

        assertEquals(1.0, WeeklyProgress.ratio(summary, goal(monday, 3.0, type = GoalType.COUNT)), 0.0)
    }

    @Test
    fun `100퍼센트를 넘어도 그대로`() {
        val summary = WeeklySummary(monday, 17_200.0, 0, 3)

        assertEquals(1.1467, WeeklyProgress.ratio(summary, goal(monday, 15_000.0)), 0.0001)
    }
}
