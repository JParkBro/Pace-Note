package jpark.bro.pacenote.feature.settings.impl.my

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.LocalDate

/**
 * 내 정보(M1, product-spec 7장). 로컬 앱 단계라 계정 · 백업 · 약관 · 앱 평가 항목은
 * 외부 설정 단계(module-map 9장 2부)에서 붙인다.
 */
internal class MyViewModel(
    statsRepository: StatsRepository,
    goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
    clock: Clock,
) : MviViewModel<MyState, MyAction, MyEvent>(MyState()) {
    init {
        combine(
            statsRepository.observeTotals(),
            goalRepository.observeGoalFor(WeeklyProgress.weekStart(LocalDate.now(clock))),
            settingsRepository.observeProfile(),
        ) { totals, goal, profile ->
            MyState(
                loading = false,
                totalDistanceM = totals.distanceM,
                totalRuns = totals.runCount,
                goal = goal,
                distanceUnit = profile.distanceUnit,
            )
        }.onEach { setState { it } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: MyAction) {
        when (action) {
            MyAction.GoalClicked -> sendEvent(MyEvent.OpenGoal)
            MyAction.ProfileClicked -> sendEvent(MyEvent.OpenProfile)
        }
    }
}
