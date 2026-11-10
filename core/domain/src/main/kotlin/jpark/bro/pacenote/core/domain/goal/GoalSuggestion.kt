package jpark.bro.pacenote.core.domain.goal

import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.Run
import java.time.LocalDate

/** 목표 기본값과 "최근 4주 평균" (product-spec 3장) */
object GoalSuggestion {
    /** 목표가 없을 때 처음 보여줄 값: 거리 15km, 횟수 주 3회 */
    fun defaultValue(type: GoalType): Double =
        when (type) {
            GoalType.DISTANCE -> DEFAULT_DISTANCE_M
            GoalType.COUNT -> DEFAULT_COUNT
        }

    /**
     * 이번 주 전 4주의 주 평균 (거리는 m, 횟수는 회).
     * 기록이 4주 이상 쌓였을 때(4주 전 주에도 기록이 있을 때)만 돌려준다.
     */
    fun recentAverage(
        runs: List<Run>,
        thisWeekStart: LocalDate,
        type: GoalType,
    ): Double? {
        val weeks = (1..WEEKS.toInt()).map { WeeklyProgress.summarize(runs, thisWeekStart.minusWeeks(it.toLong())) }
        if (weeks.last().runCount == 0) return null
        val total =
            when (type) {
                GoalType.DISTANCE -> weeks.sumOf { it.distanceM }
                GoalType.COUNT -> weeks.sumOf { it.runCount }.toDouble()
            }
        return total / WEEKS
    }

    /** 평균을 구할 때 읽을 기간의 시작 (이번 주 4주 전 월요일) */
    fun recentFrom(thisWeekStart: LocalDate): LocalDate = thisWeekStart.minusWeeks(WEEKS)

    private const val WEEKS = 4L
    private const val DEFAULT_DISTANCE_M = 15_000.0
    private const val DEFAULT_COUNT = 3.0
}
