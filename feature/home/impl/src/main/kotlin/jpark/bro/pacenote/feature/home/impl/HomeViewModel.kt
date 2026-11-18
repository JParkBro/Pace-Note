package jpark.bro.pacenote.feature.home.impl

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.LocalDate

/** 홈(H1, product-spec 5-1). 이번 주 합계 · 목표 진행 · 최근 기록 2개를 보여준다. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModel(
    runRepository: RunRepository,
    statsRepository: StatsRepository,
    goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {
    // 홈은 탭 루트라 ViewModel이 프로세스가 끝날 때까지 남는다. 주를 한 번만 읽으면 다음 주에도 지난주를 보여준다
    private val weekStart = MutableStateFlow(currentWeekStart())

    init {
        weekStart
            .flatMapLatest { weekStart ->
                combine(
                    statsRepository.observeWeeklySummary(weekStart),
                    goalRepository.observeGoalFor(weekStart),
                    runRepository.observeRecentRuns(RECENT_RUN_COUNT),
                    settingsRepository.observeProfile(),
                ) { summary, goal, runs, profile ->
                    HomeState(
                        loading = false,
                        weekStart = weekStart,
                        distanceM = summary.distanceM,
                        movingMs = summary.movingMs,
                        runCount = summary.runCount,
                        averagePaceSecPerKm = Pace.secPerKm(summary.movingMs, summary.distanceM),
                        goal = goal?.let { HomeGoal(it.type, it.value, WeeklyProgress.ratio(summary, it)) },
                        recentRuns = runs.map { it.toHomeRun() },
                        distanceUnit = profile.distanceUnit,
                    )
                }
            }.onEach { setState { it } }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.ScreenStarted -> weekStart.value = currentWeekStart()
            HomeAction.StartRunClicked -> sendEvent(HomeEvent.StartRun)
            HomeAction.SetGoalClicked -> sendEvent(HomeEvent.OpenGoal)
            is HomeAction.RunClicked -> sendEvent(HomeEvent.OpenRun(action.runId))
        }
    }

    private fun currentWeekStart(): LocalDate = WeeklyProgress.weekStart(LocalDate.now(clock))

    private fun Run.toHomeRun() =
        HomeRun(
            id = id,
            startedAt = startedAt,
            zoneId = zoneId,
            distanceM = distanceM,
            movingMs = movingMs,
            averagePaceSecPerKm = Pace.secPerKm(movingMs, distanceM),
            route = routePolyline?.let { PolylineCodec.decode(it, routeSegments) }.orEmpty(),
        )

    private companion object {
        const val RECENT_RUN_COUNT = 2
    }
}
