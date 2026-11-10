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
import jpark.bro.pacenote.core.domain.stats.RunMonth
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.Year

/** 기록 목록(R1, product-spec 5-7). 달마다 머리(합계)를 두고 최신순으로 무한 스크롤한다. */
internal class RecordViewModel(
    runRepository: RunRepository,
    statsRepository: StatsRepository,
    settingsRepository: SettingsRepository,
    clock: Clock,
) : MviViewModel<RecordState, RecordAction, RecordEvent>(RecordState()) {
    private val items: Flow<PagingData<RecordItem>> =
        runRepository
            .observeRunPages()
            .map { page -> page.map { it.toRow() }.withMonthHeaders() }
            .cachedIn(viewModelScope)

    init {
        combine(statsRepository.observeMonthlyTotals(), settingsRepository.observeProfile()) { totals, profile ->
            RecordState(
                loading = false,
                items = items,
                monthTotals = totals,
                distanceUnit = profile.distanceUnit,
                thisYear = Year.now(clock).value,
            )
        }.onEach { setState { it } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: RecordAction) {
        when (action) {
            RecordAction.StartRunClicked -> sendEvent(RecordEvent.StartRun)
        }
    }

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
}
