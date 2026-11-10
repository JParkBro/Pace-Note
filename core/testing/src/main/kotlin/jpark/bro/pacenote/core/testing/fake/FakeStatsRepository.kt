package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.model.WeeklySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

/** 주별 합계를 직접 넣는다. 넣지 않은 주는 0으로 돌려준다. */
class FakeStatsRepository : StatsRepository {
    val weeklySummaries = MutableStateFlow<Map<LocalDate, WeeklySummary>>(emptyMap())
    val totals = MutableStateFlow(RunTotals(distanceM = 0.0, runCount = 0))
    val monthlyTotals = MutableStateFlow<Map<YearMonth, RunTotals>>(emptyMap())
    val runs = MutableStateFlow<List<Run>>(emptyList())

    override fun observeWeeklySummary(weekStart: LocalDate): Flow<WeeklySummary> =
        weeklySummaries.map { it[weekStart] ?: WeeklySummary(weekStart, 0.0, 0L, 0) }

    override fun observeTotals(): Flow<RunTotals> = totals

    override fun observeMonthlyTotals(): Flow<Map<YearMonth, RunTotals>> = monthlyTotals

    /** 기간과 상관없이 [runs]를 모두 돌려준다. 기간 판단은 화면 쪽 `PeriodStats`가 한다. */
    override fun observeRunsAround(
        from: LocalDate,
        until: LocalDate,
    ): Flow<List<Run>> = runs
}
