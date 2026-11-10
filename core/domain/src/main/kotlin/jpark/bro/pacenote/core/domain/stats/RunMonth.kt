package jpark.bro.pacenote.core.domain.stats

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** 기록이 속한 달 (product-spec 5-7 기록 목록의 월 묶음) */
object RunMonth {
    /** 기록의 시간대로 시작 시각이 속한 달. 다른 시간대로 옮겨도 그 기록의 날짜가 유지된다. */
    fun of(
        startedAt: Long,
        zoneId: String,
    ): YearMonth = YearMonth.from(Instant.ofEpochMilli(startedAt).atZone(ZoneId.of(zoneId)))
}
