package jpark.bro.pacenote.feature.settings.impl.goal

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.GoalSuggestion
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.ui.MviViewModel
import jpark.bro.pacenote.core.ui.RunFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 목표(M2, product-spec 3장). 주간 거리 또는 주간 횟수 중 하나. 저장하면 이번 주부터 적용되고
 * 지난 주 목표는 그대로 남는다 (`GoalRepository.setGoal`).
 */
internal class GoalViewModel(
    private val goalRepository: GoalRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<GoalState, GoalAction, GoalEvent>(GoalState()) {
    init {
        viewModelScope.launch { load() }
    }

    override fun onAction(action: GoalAction) {
        when (action) {
            is GoalAction.TypeSelected -> onTypeSelected(action.type)
            GoalAction.DecreaseClicked -> if (currentState.canDecrease) setState { copy(amount = amount - 1) }
            GoalAction.IncreaseClicked -> if (currentState.canIncrease) setState { copy(amount = amount + 1) }
            GoalAction.SaveClicked -> save()
            GoalAction.BackClicked -> sendEvent(GoalEvent.Close)
        }
    }

    private suspend fun load() {
        val thisWeek = WeeklyProgress.weekStart(LocalDate.now(clock))
        val unit = settingsRepository.observeProfile().first().distanceUnit
        val goal = goalRepository.observeGoalFor(thisWeek).first()
        val runs = statsRepository.observeRunsAround(GoalSuggestion.recentFrom(thisWeek), thisWeek).first()
        val type = goal?.type ?: GoalType.DISTANCE
        setState {
            copy(
                loading = false,
                type = type,
                amount = toAmount(type, goal?.value ?: GoalSuggestion.defaultValue(type), unit),
                distanceUnit = unit,
                recentDistanceM = GoalSuggestion.recentAverage(runs, thisWeek, GoalType.DISTANCE),
                recentCount = GoalSuggestion.recentAverage(runs, thisWeek, GoalType.COUNT),
            )
        }
    }

    private fun onTypeSelected(type: GoalType) {
        if (type == currentState.type) return
        setState { copy(type = type, amount = toAmount(type, GoalSuggestion.defaultValue(type), distanceUnit)) }
    }

    private fun save() {
        val state = currentState
        if (state.loading || state.saving) return
        setState { copy(saving = true) }
        viewModelScope.launch {
            goalRepository
                .setGoal(state.type, toValue(state.type, state.amount, state.distanceUnit))
                .onSuccess { sendEvent(GoalEvent.Close) }
                .onFailure { sendEvent(GoalEvent.ShowError) }
            setState { copy(saving = false) }
        }
    }

    /** 저장 값(거리 m · 횟수 회) → 화면 값(km · mi 정수 · 회) */
    private fun toAmount(
        type: GoalType,
        value: Double,
        unit: DistanceUnit,
    ): Int {
        val amount =
            when (type) {
                GoalType.DISTANCE -> (value / RunFormat.unitMeters(unit)).roundToInt()
                GoalType.COUNT -> value.roundToInt()
            }
        return amount.coerceIn(GoalState.MIN_AMOUNT, GoalState.maxAmount(type))
    }

    private fun toValue(
        type: GoalType,
        amount: Int,
        unit: DistanceUnit,
    ): Double =
        when (type) {
            GoalType.DISTANCE -> amount * RunFormat.unitMeters(unit)
            GoalType.COUNT -> amount.toDouble()
        }
}
