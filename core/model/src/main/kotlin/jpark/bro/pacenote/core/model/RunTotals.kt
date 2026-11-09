package jpark.bro.pacenote.core.model

/** 누적 합계 (M1 요약 카드). 끝났고 지워지지 않은 기록만 센다. */
data class RunTotals(
    val distanceM: Double,
    val runCount: Int,
)
