package jpark.bro.pacenote.core.domain.goal

import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunStatus
import jpark.bro.pacenote.core.model.WeeklySummary
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** 주간 합계와 목표 진행률 (docs/run/product-spec.md 3장). 한 주는 월요일에 시작한다. */
object WeeklyProgress {
    fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** 기록의 시간대로 시작 시각이 속한 주를 구한다. 다른 시간대로 옮겨도 그 기록의 요일이 유지된다. */
    fun weekStartOf(run: Run): LocalDate =
        weekStart(Instant.ofEpochMilli(run.startedAt).atZone(ZoneId.of(run.zoneId)).toLocalDate())

    /** 끝났고 지워지지 않은 기록만 합산한다. */
    fun summarize(
        runs: List<Run>,
        weekStart: LocalDate,
    ): WeeklySummary {
        val inWeek =
            runs.filter { it.status == RunStatus.FINISHED && it.deletedAt == null && weekStartOf(it) == weekStart }
        return WeeklySummary(weekStart, inWeek.sumOf { it.distanceM }, inWeek.sumOf { it.movingMs }, inWeek.size)
    }

    /** 그 주에 적용되는 목표: 적용 시작 주가 그 주 이하인 것 중 가장 최근. */
    fun goalFor(
        goals: List<Goal>,
        weekStart: LocalDate,
    ): Goal? =
        goals
            .filter { it.deletedAt == null && !it.effectiveWeekStart.isAfter(weekStart) }
            .maxWithOrNull(compareBy<Goal> { it.effectiveWeekStart }.thenBy { it.updatedAt })

    /** 진행률. 100%를 넘어도 그대로 돌려준다 (1.0 = 100%). */
    fun ratio(
        summary: WeeklySummary,
        goal: Goal,
    ): Double {
        val done =
            when (goal.type) {
                GoalType.DISTANCE -> summary.distanceM
                GoalType.COUNT -> summary.runCount.toDouble()
            }
        return if (goal.value > 0) done / goal.value else 0.0
    }
}
