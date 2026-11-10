package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.common.IdGenerator
import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.data.mapper.toModel
import jpark.bro.pacenote.core.database.dao.GoalDao
import jpark.bro.pacenote.core.database.entity.GoalHistoryEntity
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/** 주간 목표 (product-spec.md 3장). 변경할 때마다 적용 시작 주와 함께 `goal_history`에 남긴다. */
interface GoalRepository {
    /** 월요일 [weekStart]로 시작하는 주에 적용되는 목표. 없으면 null */
    fun observeGoalFor(weekStart: LocalDate): Flow<Goal?>

    /**
     * 이번 주부터 적용할 목표를 정한다. 지난 주 목표는 그대로 둔다.
     * [value]는 거리 목표면 미터, 횟수 목표면 회. 0 이하면 실패.
     */
    suspend fun setGoal(
        type: GoalType,
        value: Double,
    ): Result<Unit>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultGoalRepository(
    private val goalDao: GoalDao,
    private val authRepository: AuthRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val homeWidgets: HomeWidgets,
) : GoalRepository {
    override fun observeGoalFor(weekStart: LocalDate): Flow<Goal?> =
        authRepository.observeUserId().flatMapLatest { owner ->
            goalDao.observeGoals(owner).map { goals -> WeeklyProgress.goalFor(goals.map { it.toModel() }, weekStart) }
        }

    override suspend fun setGoal(
        type: GoalType,
        value: Double,
    ): Result<Unit> =
        suspendRunCatching {
            require(value > 0) { "목표 값은 0보다 커야 한다" }
            val owner = authRepository.observeUserId().first()
            val now = clock.millis()
            val weekStart = WeeklyProgress.weekStart(LocalDate.now(clock)).toString()
            // 같은 주에 여러 번 바꾸면 행을 늘리지 않고 그 주의 행을 고친다 (백업 문서 수도 늘지 않음)
            val sameWeek = goalDao.observeGoals(owner).first().find { it.effectiveWeekStart == weekStart }
            goalDao.upsert(
                GoalHistoryEntity(
                    id = sameWeek?.id ?: idGenerator.newId(),
                    ownerUserId = owner,
                    type = type.name,
                    value = value,
                    effectiveWeekStart = weekStart,
                    createdAt = sameWeek?.createdAt ?: now,
                    updatedAt = now,
                    deletedAt = null,
                    syncState = syncStateFor(owner),
                ),
            )
        }.onSuccess { homeWidgets.requestUpdate() }
}
