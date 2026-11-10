package jpark.bro.pacenote.core.domain.stats

import jpark.bro.pacenote.core.model.ActivityType
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunSource
import jpark.bro.pacenote.core.model.RunStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class PeriodStatsTest {
    private val seoul = "Asia/Seoul"
    private val monday = LocalDate.of(2026, 10, 26)

    private fun run(
        id: String,
        dateTime: String,
        distanceM: Double,
        movingMs: Long = 1_000_000,
    ): Run {
        val startedAt =
            LocalDateTime
                .parse(dateTime)
                .atZone(ZoneId.of(seoul))
                .toInstant()
                .toEpochMilli()
        return Run(
            id = id,
            ownerUserId = null,
            status = RunStatus.FINISHED,
            startedAt = startedAt,
            endedAt = startedAt + movingMs,
            zoneId = seoul,
            distanceM = distanceM,
            movingMs = movingMs,
            elapsedMs = movingMs,
            calories = null,
            title = null,
            memo = null,
            source = RunSource.GPS,
            activityType = ActivityType.OUTDOOR_RUN,
            planId = null,
            routePolyline = null,
            routeSegments = emptyList(),
            splits = emptyList(),
            schemaVersion = 1,
            createdAt = 0,
            updatedAt = 0,
            deletedAt = null,
        )
    }

    // 샘플 시나리오(product-spec 9장): 이번 주 10.26 3.98km · 10.27 3.21km · 10.28 5.21km
    private val sampleWeek =
        listOf(
            run("1026", "2026-10-26T07:05", 3_980.0, 1_445_000),
            run("1027", "2026-10-27T20:15", 3_210.0, 1_103_000),
            run("1028", "2026-10-28T06:40", 5_210.0, 1_812_000),
        )

    @Test
    fun `주 통계는 요일별 7개 막대와 합계`() {
        val summary = PeriodStats.summarize(sampleWeek, StatsPeriod.WEEK, monday)

        assertEquals(12_400.0, summary.distanceM, 0.001)
        assertEquals(3, summary.runCount)
        assertEquals(4_360_000L, summary.movingMs)
        assertEquals(listOf(3_980.0, 3_210.0, 5_210.0, 0.0, 0.0, 0.0, 0.0), summary.bars)
    }

    @Test
    fun `일요일 23시 59분은 그 주, 다음 월요일 0시는 다음 주`() {
        val runs =
            listOf(run("sun", "2026-11-01T23:59", 1_000.0), run("mon", "2026-11-02T00:00", 2_000.0))

        val summary = PeriodStats.summarize(runs, StatsPeriod.WEEK, monday)

        assertEquals(1, summary.runCount)
        assertEquals(1_000.0, summary.bars.last(), 0.0)
    }

    @Test
    fun `버린 기록과 지운 기록은 넣지 않음`() {
        val runs =
            sampleWeek +
                run("discarded", "2026-10-29T07:00", 9_000.0).copy(status = RunStatus.DISCARDED) +
                run("deleted", "2026-10-29T07:00", 9_000.0).copy(deletedAt = 1L)

        assertEquals(3, PeriodStats.summarize(runs, StatsPeriod.WEEK, monday).runCount)
    }

    @Test
    fun `달 통계 막대는 그 달이 걸친 주마다 하나`() {
        // 2026년 10월: 1일(목)이 9.28 주, 31일(토)이 10.26 주 → 5주
        val october = LocalDate.of(2026, 10, 1)
        val runs = sampleWeek + run("1001", "2026-10-01T07:00", 5_000.0) + run("0930", "2026-09-30T07:00", 7_000.0)

        val summary = PeriodStats.summarize(runs, StatsPeriod.MONTH, october)

        assertEquals(LocalDate.of(2026, 9, 28), summary.barStarts.first())
        assertEquals(5, summary.bars.size)
        assertEquals(5_000.0, summary.bars.first(), 0.0)
        assertEquals(12_400.0, summary.bars.last(), 0.001)
        assertEquals(4, summary.runCount)
    }

    @Test
    fun `기간 시작 · 이동`() {
        val thursday = LocalDate.of(2026, 10, 29)

        assertEquals(monday, PeriodStats.startOf(StatsPeriod.WEEK, thursday))
        assertEquals(LocalDate.of(2026, 10, 1), PeriodStats.startOf(StatsPeriod.MONTH, thursday))
        assertEquals(LocalDate.of(2026, 10, 19), PeriodStats.shift(StatsPeriod.WEEK, monday, -1))
        assertEquals(LocalDate.of(2026, 11, 1), PeriodStats.endOf(StatsPeriod.MONTH, LocalDate.of(2026, 10, 1)))
    }
}
