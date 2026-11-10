package jpark.bro.pacenote.widget

import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/** 위젯이 그릴 이번 주 값을 Repository에서 읽는다 (widget/CLAUDE.md: `core:database`를 직접 읽지 않음). */
internal class WidgetDataSource(
    private val statsRepository: StatsRepository,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) {
    suspend fun load(): WeeklyWidgetData {
        val weekStart = WeeklyProgress.weekStart(LocalDate.now(clock))
        return WeeklyWidgetData.of(
            summary = statsRepository.observeWeeklySummary(weekStart).first(),
            goal = goalRepository.observeGoalFor(weekStart).first(),
            unit = settingsRepository.observeProfile().first().distanceUnit,
            themeType = settingsRepository.observeEffectiveThemeSetting().first().type,
        )
    }
}
