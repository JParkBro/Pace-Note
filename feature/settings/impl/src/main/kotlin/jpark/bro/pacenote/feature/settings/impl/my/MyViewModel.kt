package jpark.bro.pacenote.feature.settings.impl.my

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/**
 * 내 정보(M1, product-spec 7장)와 테마 시트(M5). 로컬 앱 단계라 계정 · 백업 · 약관 · 앱 평가 ·
 * 앱 정보(최신 여부) 항목은 외부 설정 단계(module-map 9장 2부)에서 붙인다.
 */
internal class MyViewModel(
    statsRepository: StatsRepository,
    goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
    clock: Clock,
) : MviViewModel<MyState, MyAction, MyEvent>(MyState()) {
    init {
        combine(
            statsRepository.observeTotals(),
            goalRepository.observeGoalFor(WeeklyProgress.weekStart(LocalDate.now(clock))),
            settingsRepository.observeProfile(),
        ) { totals, goal, profile ->
            setState {
                copy(
                    loading = false,
                    totalDistanceM = totals.distanceM,
                    totalRuns = totals.runCount,
                    goal = goal,
                    distanceUnit = profile.distanceUnit,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: MyAction) {
        when (action) {
            MyAction.GoalClicked -> sendEvent(MyEvent.OpenGoal)
            MyAction.RunSettingsClicked -> sendEvent(MyEvent.OpenRunSettings)
            MyAction.ProfileClicked -> sendEvent(MyEvent.OpenProfile)
            MyAction.ThemeClicked -> openThemeSheet()
            is MyAction.ThemeTypeSelected -> previewTheme { copy(type = action.type) }
            is MyAction.ThemeModeSelected -> previewTheme { copy(mode = action.mode) }
            MyAction.ThemeApplyClicked -> applyTheme()
            MyAction.ThemeSheetDismissed -> closeThemeSheet()
            MyAction.ContactClicked -> sendEvent(MyEvent.OpenContact)
        }
    }

    // 테마 시트(M5): 고르는 즉시 앱 전체에 미리보기, 적용하면 저장, 닫으면 원래대로 (product-spec 7장)

    private fun openThemeSheet() {
        viewModelScope.launch {
            val saved = settingsRepository.observeEffectiveThemeSetting().first()
            setState { copy(themeSheet = saved) }
        }
    }

    private fun previewTheme(change: ThemeSetting.() -> ThemeSetting) {
        val next = currentState.themeSheet?.change() ?: return
        setState { copy(themeSheet = next) }
        settingsRepository.previewThemeSetting(next)
    }

    private fun applyTheme() {
        val selected = currentState.themeSheet ?: return
        viewModelScope.launch {
            settingsRepository
                .applyThemeSetting(selected)
                .onSuccess { closeThemeSheet() }
                .onFailure { sendEvent(MyEvent.ShowError) }
        }
    }

    private fun closeThemeSheet() {
        settingsRepository.previewThemeSetting(null)
        setState { copy(themeSheet = null) }
    }

    /** 시트를 연 채로 화면을 떠나도 미리보기가 남지 않게 한다 */
    override fun onCleared() {
        if (currentState.themeSheet != null) settingsRepository.previewThemeSetting(null)
        super.onCleared()
    }
}
