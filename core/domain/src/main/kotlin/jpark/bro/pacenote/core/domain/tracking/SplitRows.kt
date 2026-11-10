package jpark.bro.pacenote.core.domain.tracking

import jpark.bro.pacenote.core.model.RunSplit

/**
 * 구간 표 한 줄 (결과 P6 · 상세 R3). 구간은 항상 km 단위다 (tracking.md 5장).
 *
 * @property endDistanceM 이 구간이 끝난 누적 거리. 마지막 짧은 구간은 1km 배수가 아니다
 */
data class SplitRow(
    val index: Int,
    val endDistanceM: Double,
    val paceSecPerKm: Double?,
)

object SplitRows {
    /** 저장된 구간을 순서대로 누적 거리와 페이스로 바꾼다. 마지막 짧은 구간(10m 이상만 저장됨)도 환산 페이스를 구한다. */
    fun of(splits: List<RunSplit>): List<SplitRow> {
        var endM = 0.0
        return splits.sortedBy { it.index }.map { split ->
            endM += split.distanceM
            SplitRow(split.index, endM, Pace.secPerKm(split.movingMs, split.distanceM, minDistanceM = 0.0))
        }
    }
}
