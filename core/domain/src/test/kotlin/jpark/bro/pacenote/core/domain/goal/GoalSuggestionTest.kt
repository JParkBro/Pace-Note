package jpark.bro.pacenote.core.domain.goal

import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GoalSuggestionTest {
    private val thisWeek = LocalDate.of(2026, 10, 26)

    private fun run(
        date: LocalDate,
        distanceM: Double,
    ): Run {
        val startedAt =
            date
                .atTime(7, 0)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli()
        return Run(
            id = date.toString(),
            ownerUserId = null,
            status = RunStatus.FINISHED,
            startedAt = startedAt,
            endedAt = startedAt + 1_000,
            zoneId = "Asia/Seoul",
            distanceM = distanceM,
            movingMs = 1_000,
            elapsedMs = 1_000,
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
            deletedAt = null,
        )
    }

    @Test
    fun `지난 4주 주 평균, 이번 주 기록은 넣지 않음`() {
        val runs =
            listOf(
                run(LocalDate.of(2026, 9, 28), 10_000.0),
                run(LocalDate.of(2026, 10, 5), 12_000.0),
                run(LocalDate.of(2026, 10, 12), 14_000.0),
                run(LocalDate.of(2026, 10, 19), 15_200.0),
                run(LocalDate.of(2026, 10, 20), 0.0),
                run(LocalDate.of(2026, 10, 27), 99_000.0),
            )

        assertEquals(12_800.0, GoalSuggestion.recentAverage(runs, thisWeek, GoalType.DISTANCE)!!, 0.001)
        assertEquals(1.25, GoalSuggestion.recentAverage(runs, thisWeek, GoalType.COUNT)!!, 0.001)
    }

    @Test
    fun `4주 전 주에 기록이 없으면 평균을 보여주지 않음`() {
        val runs = listOf(run(LocalDate.of(2026, 10, 19), 5_000.0))

        assertNull(GoalSuggestion.recentAverage(runs, thisWeek, GoalType.DISTANCE))
    }

    @Test
    fun `기본 추천은 15km 또는 주 3회`() {
        assertEquals(15_000.0, GoalSuggestion.defaultValue(GoalType.DISTANCE), 0.0)
        assertEquals(3.0, GoalSuggestion.defaultValue(GoalType.COUNT), 0.0)
        assertEquals(LocalDate.of(2026, 9, 28), GoalSuggestion.recentFrom(thisWeek))
    }
}
