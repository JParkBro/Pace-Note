package jpark.bro.pacenote.feature.record.impl.list

import androidx.paging.PagingData
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.model.RunTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.YearMonth

/**
 * @property items 월 머리 + 기록, 최신순 페이지. 화면이 사라져도 이어 보도록 ViewModel에 캐시된 흐름이다
 * @property monthTotals 월 머리에 보여줄 달별 합계. 비어 있으면 기록이 없다
 */
internal data class RecordState(
    val loading: Boolean = true,
    val items: Flow<PagingData<RecordItem>> = emptyFlow(),
    val monthTotals: Map<YearMonth, RunTotals> = emptyMap(),
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    /** 올해가 아닌 달은 월 머리에 연도를 붙인다 */
    val thisYear: Int = 0,
) {
    val isEmpty: Boolean get() = !loading && monthTotals.isEmpty()
}

internal sealed interface RecordItem {
    val key: String

    data class MonthHeader(
        val month: YearMonth,
    ) : RecordItem {
        override val key: String get() = "month-$month"
    }

    data class RunRow(
        val id: String,
        val month: YearMonth,
        val startedAt: Long,
        val zoneId: String,
        val distanceM: Double,
        val movingMs: Long,
        val averagePaceSecPerKm: Double?,
        val route: List<RoutePoint>,
    ) : RecordItem {
        override val key: String get() = "run-$id"
    }
}
