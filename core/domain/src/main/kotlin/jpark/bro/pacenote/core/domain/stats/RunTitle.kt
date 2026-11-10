package jpark.bro.pacenote.core.domain.stats

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

/** 하루 중 때 (기본 제목 "{요일} {때} 러닝", product-spec 5-8) */
enum class TimeOfDay {
    /** 05:00 ~ 10:59 */
    MORNING,

    /** 11:00 ~ 16:59 */
    MIDDAY,

    /** 17:00 ~ 20:59 */
    EVENING,

    /** 21:00 ~ 04:59 */
    NIGHT,
}

/** 제목을 비워 둔 기록에 보여줄 기본 제목의 재료. 문장은 화면이 문자열 리소스로 만든다. */
data class DefaultRunTitle(
    val dayOfWeek: DayOfWeek,
    val timeOfDay: TimeOfDay,
)

object RunTitle {
    private const val MORNING_FROM = 5
    private const val MIDDAY_FROM = 11
    private const val EVENING_FROM = 17
    private const val NIGHT_FROM = 21

    /** 기록의 시간대로 시작 시각의 요일과 때를 구한다. 밤 0 ~ 5시는 그 날짜의 요일이다. */
    fun defaultOf(
        startedAt: Long,
        zoneId: String,
    ): DefaultRunTitle {
        val local = Instant.ofEpochMilli(startedAt).atZone(ZoneId.of(zoneId))
        val timeOfDay =
            when (local.hour) {
                in MORNING_FROM until MIDDAY_FROM -> TimeOfDay.MORNING
                in MIDDAY_FROM until EVENING_FROM -> TimeOfDay.MIDDAY
                in EVENING_FROM until NIGHT_FROM -> TimeOfDay.EVENING
                else -> TimeOfDay.NIGHT
            }
        return DefaultRunTitle(local.dayOfWeek, timeOfDay)
    }
}
