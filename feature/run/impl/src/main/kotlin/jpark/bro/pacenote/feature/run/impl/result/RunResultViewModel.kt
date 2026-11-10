package jpark.bro.pacenote.feature.run.impl.result

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.domain.tracking.SplitRows
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.Run
import jpark.bro.pacenote.core.model.WeeklySummary
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * 결과(P6, product-spec 5-5). 기록은 P5에서 이미 저장되어 있고, 여기서는 보여주고 메모만 고친다.
 * 메모는 입력이 [MEMO_SAVE_DELAY] 동안 멈추면 저장하고, "완료"를 누르면 남은 것을 바로 저장한다.
 */
internal class RunResultViewModel(
    private val runId: String,
    private val runRepository: RunRepository,
    private val statsRepository: StatsRepository,
    private val goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
) : MviViewModel<RunResultState, RunResultAction, RunResultEvent>(RunResultState()) {
    private var title: String? = null
    private var savedMemo = ""
    private var memoJob: Job? = null

    init {
        settingsRepository
            .observeProfile()
            .onEach { setState { copy(distanceUnit = it.distanceUnit) } }
            .launchIn(viewModelScope)
        viewModelScope.launch { load() }
    }

    override fun onAction(action: RunResultAction) {
        when (action) {
            is RunResultAction.MemoChanged -> onMemoChanged(action.memo)
            RunResultAction.DoneClicked -> onDone()
        }
    }

    private suspend fun load() {
        val run = runRepository.observeRun(runId).first()
        if (run == null) {
            sendEvent(RunResultEvent.Finish)
            return
        }
        title = run.title
        savedMemo = run.memo.orEmpty()
        setState { withRun(run) }
        val weekStart = WeeklyProgress.weekStartOf(run)
        combine(statsRepository.observeWeeklySummary(weekStart), goalRepository.observeGoalFor(weekStart)) {
            summary,
            goal,
            ->
            goal?.let { resultGoal(summary, it) }
        }.collect { goal -> setState { copy(weeklyGoal = goal) } }
    }

    private fun onMemoChanged(memo: String) {
        if (currentState.loading) return
        setState { copy(memo = memo.take(RunResultState.MEMO_MAX_LENGTH)) }
        memoJob?.cancel()
        memoJob =
            viewModelScope.launch {
                delay(MEMO_SAVE_DELAY)
                saveMemo()
            }
    }

    private fun onDone() {
        memoJob?.cancel()
        viewModelScope.launch {
            saveMemo()
            sendEvent(RunResultEvent.Finish)
        }
    }

    private suspend fun saveMemo() {
        val memo = currentState.memo
        if (currentState.loading || memo == savedMemo) return
        runRepository
            .updateTitleAndMemo(runId, title, memo)
            .onSuccess { savedMemo = memo }
            .onFailure { sendEvent(RunResultEvent.ShowError) }
    }

    private fun RunResultState.withRun(run: Run): RunResultState =
        copy(
            loading = false,
            startedAt = run.startedAt,
            zoneId = run.zoneId,
            distanceM = run.distanceM,
            movingMs = run.movingMs,
            averagePaceSecPerKm = Pace.secPerKm(run.movingMs, run.distanceM),
            calories = run.calories,
            route = run.routePolyline?.let { PolylineCodec.decode(it, run.routeSegments) }.orEmpty(),
            splits = SplitRows.of(run.splits),
            memo = run.memo.orEmpty(),
        )

    private fun resultGoal(
        summary: WeeklySummary,
        goal: Goal,
    ): ResultGoal {
        val ratio = WeeklyProgress.ratio(summary, goal)
        return ResultGoal(goal.type, ratio, (goal.value * (1 - ratio)).coerceAtLeast(0.0))
    }

    private companion object {
        val MEMO_SAVE_DELAY = 500.milliseconds
    }
}
