package jpark.bro.pacenote.feature.onboarding.impl.goal

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.OnboardingRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.domain.goal.GoalSuggestion
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.ui.MviViewModel
import jpark.bro.pacenote.core.ui.RunFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 첫 목표(O3): 주간 거리 15km 또는 주 3회부터. [시작하기]는 목표를 저장하고, 건너뛰면 목표 없이 온보딩을 끝낸다.
 * 어느 쪽이든 온보딩 완료를 저장해 다음 실행부터는 홈으로 시작한다.
 */
internal class FirstGoalViewModel(
    private val goalRepository: GoalRepository,
    private val onboardingRepository: OnboardingRepository,
    private val settingsRepository: SettingsRepository,
) : MviViewModel<FirstGoalState, FirstGoalAction, FirstGoalEvent>(FirstGoalState()) {
    init {
        viewModelScope.launch {
            val unit = settingsRepository.observeProfile().first().distanceUnit
            setState { copy(loading = false, distanceUnit = unit, amount = defaultAmount(GoalType.DISTANCE, unit)) }
        }
    }

    override fun onAction(action: FirstGoalAction) {
        when (action) {
            is FirstGoalAction.TypeSelected -> onTypeSelected(action.type)
            FirstGoalAction.DecreaseClicked -> if (currentState.canDecrease) setState { copy(amount = amount - 1) }
            FirstGoalAction.IncreaseClicked -> if (currentState.canIncrease) setState { copy(amount = amount + 1) }
            FirstGoalAction.StartClicked -> finish(saveGoal = true)
            FirstGoalAction.SkipClicked -> finish(saveGoal = false)
            FirstGoalAction.BackClicked -> sendEvent(FirstGoalEvent.NavigateBack)
        }
    }

    private fun onTypeSelected(type: GoalType) {
        if (type == currentState.type) return
        setState { copy(type = type, amount = defaultAmount(type, distanceUnit)) }
    }

    private fun finish(saveGoal: Boolean) {
        val state = currentState
        if (state.loading || state.working) return
        setState { copy(working = true) }
        viewModelScope.launch {
            val goal =
                if (saveGoal) {
                    goalRepository.setGoal(state.type, toValue(state.type, state.amount, state.distanceUnit))
                } else {
                    Result.success(Unit)
                }
            val done = if (goal.isSuccess) onboardingRepository.completeOnboarding() else goal
            done
                .onSuccess { sendEvent(FirstGoalEvent.Done) }
                .onFailure { sendEvent(FirstGoalEvent.ShowError) }
            setState { copy(working = false) }
        }
    }

    private fun defaultAmount(
        type: GoalType,
        unit: DistanceUnit,
    ): Int {
        val value = GoalSuggestion.defaultValue(type)
        val amount =
            when (type) {
                GoalType.DISTANCE -> (value / RunFormat.unitMeters(unit)).roundToInt()
                GoalType.COUNT -> value.roundToInt()
            }
        return amount.coerceIn(FirstGoalState.MIN_AMOUNT, FirstGoalState.maxAmount(type))
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
