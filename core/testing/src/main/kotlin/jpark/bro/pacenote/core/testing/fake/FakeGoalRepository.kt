package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

/** 어느 주를 물어도 [goal]을 돌려준다. [setGoal]은 마지막 값을 [lastSet]에 남긴다. */
class FakeGoalRepository : GoalRepository {
    val goal = MutableStateFlow<Goal?>(null)
    var writeResult: Result<Unit> = Result.success(Unit)
    var lastSet: Pair<GoalType, Double>? = null
        private set

    override fun observeGoalFor(weekStart: LocalDate): Flow<Goal?> = goal

    override suspend fun setGoal(
        type: GoalType,
        value: Double,
    ): Result<Unit> = writeResult.onSuccess { lastSet = type to value }
}
