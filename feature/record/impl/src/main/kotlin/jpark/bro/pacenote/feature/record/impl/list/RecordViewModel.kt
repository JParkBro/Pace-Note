package jpark.bro.pacenote.feature.record.impl.list

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.stats.PeriodStats
import jpark.bro.pacenote.core.domain.stats.RunMonth
import jpark.bro.pacenote.core.domain.stats.StatsPeriod
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.Year

/**
 * 기록 탭 (product-spec 5-7).
 * - 목록(R1): 달마다 머리(합계)를 두고 최신순으로 무한 스크롤한다.
 * - 통계(R2): 주 / 월 기간의 막대 · 합계 · 지난 기간 대비. 기간 계산은 `PeriodStats`가 한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class RecordViewModel(
    runRepository: RunRepository,
    private val statsRepository: StatsRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<RecordState, RecordAction, RecordEvent>(RecordState()) {
    private val items: Flow<PagingData<RecordItem>> =
        runRepository
            .observeRunPages()
            .map { page -> page.map { it.toRow() }.withMonthHeaders() }
            .cachedIn(viewModelScope)

    private val selectedPeriod = MutableStateFlow(selectionOf(StatsPeriod.WEEK))

    init {
        combine(statsRepository.observeMonthlyTotals(), settingsRepository.observeProfile()) { totals, profile ->
            setState {
                copy(
                    loading = false,
                    items = this@RecordViewModel.items,
                    monthTotals = totals,
                    distanceUnit = profile.distanceUnit,
                    thisYear = Year.now(clock).value,
                )
            }
        }.launchIn(viewModelScope)
        selectedPeriod
            .flatMapLatest { selection -> observeStats(selection) }
            .onEach { stats -> setState { copy(stats = stats) } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RecordAction) {
        when (action) {
            RecordAction.StartRunClicked -> sendEvent(RecordEvent.StartRun)
            is RecordAction.TabSelected -> setState { copy(tab = action.tab) }
            is RecordAction.PeriodSelected -> selectedPeriod.value = selectionOf(action.period)
            RecordAction.PreviousPeriodClicked -> movePeriod(-1)
            RecordAction.NextPeriodClicked -> if (currentState.stats?.canGoNext == true) movePeriod(1)
        }
    }

    private fun movePeriod(count: Long) {
        selectedPeriod.update { it.copy(start = PeriodStats.shift(it.period, it.start, count)) }
    }

    private fun observeStats(selection: StatsSelection): Flow<RecordStats> {
        val (period, start) = selection
        val previousStart = PeriodStats.shift(period, start, -1)
        return statsRepository.observeRunsAround(previousStart, PeriodStats.endOf(period, start)).map { runs ->
            val current = PeriodStats.summarize(runs, period, start)
            val today = today()
            RecordStats(
                current = current,
                previousDistanceM = PeriodStats.summarize(runs, period, previousStart).distanceM,
                averagePaceSecPerKm = Pace.secPerKm(current.movingMs, current.distanceM),
                today = today,
                canGoNext = start.isBefore(PeriodStats.startOf(period, today)),
            )
        }
    }

    private fun today(): LocalDate = LocalDate.now(clock)

    private fun selectionOf(period: StatsPeriod) = StatsSelection(period, PeriodStats.startOf(period, today()))

    private fun Run.toRow(): RecordItem.RunRow =
        RecordItem.RunRow(
            id = id,
            month = RunMonth.of(startedAt, zoneId),
            startedAt = startedAt,
            zoneId = zoneId,
            distanceM = distanceM,
            movingMs = movingMs,
            averagePaceSecPerKm = Pace.secPerKm(movingMs, distanceM),
            route = routePolyline?.let { PolylineCodec.decode(it, routeSegments) }.orEmpty(),
        )

    /** 달이 바뀌는 곳(맨 앞 포함)에 월 머리를 넣는다. */
    private fun PagingData<RecordItem.RunRow>.withMonthHeaders(): PagingData<RecordItem> =
        insertSeparators<RecordItem.RunRow, RecordItem> { before, after ->
            if (after != null && before?.month != after.month) RecordItem.MonthHeader(after.month) else null
        }

    /** [start]는 그 기간의 첫날 */
    private data class StatsSelection(
        val period: StatsPeriod,
        val start: LocalDate,
    )
}
