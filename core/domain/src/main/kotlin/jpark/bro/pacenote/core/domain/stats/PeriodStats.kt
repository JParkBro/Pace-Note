package jpark.bro.pacenote.core.domain.stats

import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** 통계(R2)의 기간 */
enum class StatsPeriod {
    /** 월요일 ~ 일요일. 막대는 요일별 */
    WEEK,

    /** 1일 ~ 말일. 막대는 그 달이 걸친 주(월요일 시작)별 */
    MONTH,
}

/**
 * 한 기간의 합계와 막대 값.
 *
 * @property bars 막대마다의 거리(m). 주는 월 ~ 일 7개, 달은 그 달이 걸친 주 수만큼
 * @property barStarts 막대마다 시작 날짜 (주: 그 날, 달: 그 주의 월요일)
 */
data class PeriodSummary(
    val period: StatsPeriod,
    val start: LocalDate,
    val distanceM: Double,
    val movingMs: Long,
    val runCount: Int,
    val bars: List<Double>,
    val barStarts: List<LocalDate>,
)

/** 주 · 달 통계 (product-spec 5-7). 날짜는 기록의 시간대로 정한다. */
object PeriodStats {
    private const val DAYS_IN_WEEK = 7L

    /** [date]가 속한 기간의 첫날 */
    fun startOf(
        period: StatsPeriod,
        date: LocalDate,
    ): LocalDate =
        when (period) {
            StatsPeriod.WEEK -> WeeklyProgress.weekStart(date)
            StatsPeriod.MONTH -> date.withDayOfMonth(1)
        }

    /** 기간이 끝난 다음 날 (포함하지 않음) */
    fun endOf(
        period: StatsPeriod,
        start: LocalDate,
    ): LocalDate = shift(period, start, 1)

    /** [start]에서 [count]개 기간만큼 옮긴 기간의 첫날. 음수면 이전 */
    fun shift(
        period: StatsPeriod,
        start: LocalDate,
        count: Long,
    ): LocalDate =
        when (period) {
            StatsPeriod.WEEK -> start.plusWeeks(count)
            StatsPeriod.MONTH -> start.plusMonths(count)
        }

    /** 끝났고 지워지지 않은 기록 중 기간 안에 시작한 것만 합산한다. */
    fun summarize(
        runs: List<Run>,
        period: StatsPeriod,
        start: LocalDate,
    ): PeriodSummary {
        val end = endOf(period, start)
        val inPeriod =
            runs
                .filter { it.status == RunStatus.FINISHED && it.deletedAt == null }
                .map { it to dateOf(it) }
                .filter { (_, date) -> !date.isBefore(start) && date.isBefore(end) }
        val barStarts = barStarts(period, start, end)
        val bars =
            barStarts.map { barStart ->
                inPeriod.filter { (_, date) -> barOf(period, date) == barStart }.sumOf { (run, _) -> run.distanceM }
            }
        return PeriodSummary(
            period = period,
            start = start,
            distanceM = inPeriod.sumOf { (run, _) -> run.distanceM },
            movingMs = inPeriod.sumOf { (run, _) -> run.movingMs },
            runCount = inPeriod.size,
            bars = bars,
            barStarts = barStarts,
        )
    }

    private fun dateOf(run: Run): LocalDate =
        Instant.ofEpochMilli(run.startedAt).atZone(ZoneId.of(run.zoneId)).toLocalDate()

    private fun barStarts(
        period: StatsPeriod,
        start: LocalDate,
        end: LocalDate,
    ): List<LocalDate> =
        when (period) {
            StatsPeriod.WEEK -> {
                List(DAYS_IN_WEEK.toInt()) { start.plusDays(it.toLong()) }
            }

            StatsPeriod.MONTH -> {
                val firstWeek = WeeklyProgress.weekStart(start)
                val weeks = ChronoUnit.WEEKS.between(firstWeek, WeeklyProgress.weekStart(end.minusDays(1))) + 1
                List(weeks.toInt()) { firstWeek.plusWeeks(it.toLong()) }
            }
        }

    private fun barOf(
        period: StatsPeriod,
        date: LocalDate,
    ): LocalDate =
        when (period) {
            StatsPeriod.WEEK -> date
            StatsPeriod.MONTH -> WeeklyProgress.weekStart(date)
        }
}
