package jpark.bro.pacenote.feature.record.impl.detail

import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle
import jpark.bro.pacenote.core.domain.tracking.SplitRow
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RoutePoint

/**
 * @property missing 기록이 없거나 삭제됐다
 * @property title 사용자가 정한 제목. null이면 [defaultTitle]을 보여준다
 */
internal data class RecordDetailState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String? = null,
    val defaultTitle: DefaultRunTitle? = null,
    val startedAt: Long = 0L,
    val endedAt: Long? = null,
    val zoneId: String = "UTC",
    val distanceM: Double = 0.0,
    val movingMs: Long = 0L,
    val elapsedMs: Long = 0L,
    val averagePaceSecPerKm: Double? = null,
    val calories: Int? = null,
    val route: List<RoutePoint> = emptyList(),
    val splits: List<SplitRow> = emptyList(),
    val memo: String? = null,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
)
