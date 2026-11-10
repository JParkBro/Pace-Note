package jpark.bro.pacenote.widget

import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.WeeklySummary

/**
 * 주간 목표 위젯이 그리는 값 (product-spec 8장). 계산은 `WeeklyProgress`를 그대로 쓴다.
 *
 * @property goalValue 거리 목표면 m, 횟수 목표면 회. 목표가 없으면 null (이번 주 거리만 보여준다)
 * @property ratio 1.0 = 100%. 넘으면 그대로 (진행바 · 링은 가득 찬 채로)
 */
internal data class WeeklyWidgetData(
    val distanceM: Double,
    val runCount: Int,
    val goalType: GoalType?,
    val goalValue: Double?,
    val ratio: Double,
    val distanceUnit: DistanceUnit,
    val themeType: AppThemeType,
) {
    companion object {
        fun of(
            summary: WeeklySummary,
            goal: Goal?,
            unit: DistanceUnit,
            themeType: AppThemeType,
        ): WeeklyWidgetData =
            WeeklyWidgetData(
                distanceM = summary.distanceM,
                runCount = summary.runCount,
                goalType = goal?.type,
                goalValue = goal?.value,
                ratio = goal?.let { WeeklyProgress.ratio(summary, it) } ?: 0.0,
                distanceUnit = unit,
                themeType = themeType,
            )
    }
}
