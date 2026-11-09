package jpark.bro.pacenote.core.ui

import jpark.bro.pacenote.core.model.DistanceUnit
import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * 거리 · 시간 · 페이스 표기를 만드는 유일한 곳 (product-spec.md 2-2, theme-guide.md 11장).
 * 숫자만 돌려주고 단위 글자(km · mi)는 [unitLabelRes]의 문자열 리소스로 붙인다.
 */
object RunFormat {
    /** 페이스가 없을 때 (정지 · 데이터 부족 · 걷기보다 느림) */
    const val NO_PACE = "–′––″"

    private const val METERS_PER_KM = 1_000.0
    private const val METERS_PER_MILE = 1_609.344
    private const val HUNDREDTHS = 100
    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 3_600L
    private const val MS_PER_SECOND = 1_000L

    /** 소수 둘째 자리까지, 내림: 5,219m → "5.21" */
    fun distance(
        meters: Double,
        unit: DistanceUnit,
    ): String {
        // 5.21처럼 이진수로 정확히 나타낼 수 없는 값이 5.2099…로 내려가지 않게 작은 여유를 둔다
        val hundredths = floor(meters / unitMeters(unit) * HUNDREDTHS + EPSILON).toLong().coerceAtLeast(0)
        return "%d.%02d".format(hundredths / HUNDREDTHS, hundredths % HUNDREDTHS)
    }

    /** 운동 시간: 1시간 미만 "30:12", 이상 "1:12:40". 초 미만은 버림 */
    fun duration(ms: Long): String {
        val totalSeconds = ms.coerceAtLeast(0) / MS_PER_SECOND
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = totalSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
    }

    /** km당 초 → 단위당 "5′48″". null이면 [NO_PACE]. 초는 반올림 */
    fun pace(
        secPerKm: Double?,
        unit: DistanceUnit,
    ): String {
        if (secPerKm == null || secPerKm <= 0 || secPerKm.isNaN()) return NO_PACE
        val totalSeconds = (secPerKm * unitMeters(unit) / METERS_PER_KM).roundToLong()
        return "%d′%02d″".format(totalSeconds / SECONDS_PER_MINUTE, totalSeconds % SECONDS_PER_MINUTE)
    }

    fun unitLabelRes(unit: DistanceUnit): Int =
        when (unit) {
            DistanceUnit.KM -> R.string.ui_unit_km
            DistanceUnit.MILE -> R.string.ui_unit_mile
        }

    private fun unitMeters(unit: DistanceUnit): Double =
        when (unit) {
            DistanceUnit.KM -> METERS_PER_KM
            DistanceUnit.MILE -> METERS_PER_MILE
        }

    private const val EPSILON = 1e-9
}
