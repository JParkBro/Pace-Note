package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.data.mapper.toModel
import jpark.bro.pacenote.core.database.dao.RunDao
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.domain.stats.RunMonth
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.model.WeeklySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

/** 홈 · 내 정보의 합계. 주 경계는 기록의 시간대로 판단한다 (`WeeklyProgress`). */
interface StatsRepository {
    /** 월요일 [weekStart]로 시작하는 주의 합계 */
    fun observeWeeklySummary(weekStart: LocalDate): Flow<WeeklySummary>

    fun observeTotals(): Flow<RunTotals>

    /** 달별 거리 · 횟수 합계 (R1 월 헤더). 기록이 없는 달은 들어 있지 않다. */
    fun observeMonthlyTotals(): Flow<Map<YearMonth, RunTotals>>

    /**
     * [from] ~ [until](포함하지 않음) 무렵에 시작한 끝난 기록 (R2 통계).
     * 기록마다 시간대가 달라 앞뒤로 하루씩 넓게 돌려준다. 기간 판단은 `PeriodStats`에 맡긴다.
     */
    fun observeRunsAround(
        from: LocalDate,
        until: LocalDate,
    ): Flow<List<Run>>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultStatsRepository(
    private val runDao: RunDao,
    private val authRepository: AuthRepository,
    private val clock: Clock,
) : StatsRepository {
    override fun observeWeeklySummary(weekStart: LocalDate): Flow<WeeklySummary> =
        observeRunsAround(weekStart, weekStart.plusDays(DAYS_IN_WEEK)).map { runs ->
            WeeklyProgress.summarize(runs, weekStart)
        }

    override fun observeRunsAround(
        from: LocalDate,
        until: LocalDate,
    ): Flow<List<Run>> {
        // 기록마다 시간대가 달라 기기 시간대 기준 앞뒤 하루씩 넓게 읽는다. 정확한 기간 판단은 core:domain이 한다
        val zone = clock.zone
        val fromMs =
            from
                .minusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        val untilMs =
            until
                .plusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        return authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeInRange(owner, fromMs, untilMs).map { runs -> runs.map { it.toModel() } }
        }
    }

    override fun observeTotals(): Flow<RunTotals> =
        authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeTotals(owner).map { it.toModel() }
        }

    override fun observeMonthlyTotals(): Flow<Map<YearMonth, RunTotals>> =
        authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeMonthRows(owner).map { rows ->
                rows
                    .groupBy { RunMonth.of(it.startedAt, it.zoneId) }
                    .mapValues { (_, inMonth) -> RunTotals(inMonth.sumOf { it.distanceM }, inMonth.size) }
            }
        }

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
