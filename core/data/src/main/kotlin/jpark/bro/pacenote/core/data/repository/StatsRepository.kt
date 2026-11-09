package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.data.mapper.toModel
import jpark.bro.pacenote.core.database.dao.RunDao
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.model.WeeklySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/** 홈 · 내 정보의 합계. 주 경계는 기록의 시간대로 판단한다 (`WeeklyProgress`). */
interface StatsRepository {
    /** 월요일 [weekStart]로 시작하는 주의 합계 */
    fun observeWeeklySummary(weekStart: LocalDate): Flow<WeeklySummary>

    fun observeTotals(): Flow<RunTotals>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultStatsRepository(
    private val runDao: RunDao,
    private val authRepository: AuthRepository,
    private val clock: Clock,
) : StatsRepository {
    override fun observeWeeklySummary(weekStart: LocalDate): Flow<WeeklySummary> {
        // 기록마다 시간대가 달라 기기 시간대 기준 앞뒤 하루씩 넓게 읽고, 주 판단은 WeeklyProgress에 맡긴다
        val zone = clock.zone
        val fromMs =
            weekStart
                .minusDays(1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        val untilMs =
            weekStart
                .plusDays(DAYS_IN_WEEK + 1)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        return authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeInRange(owner, fromMs, untilMs).map { runs ->
                WeeklyProgress.summarize(runs.map { it.toModel() }, weekStart)
            }
        }
    }

    override fun observeTotals(): Flow<RunTotals> =
        authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeTotals(owner).map { it.toModel() }
        }

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
