package jpark.bro.pacenote.feature.record.impl.detail

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.stats.RunTitle
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.domain.tracking.SplitRows
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/** 기록 상세(R3, product-spec 5-8). 결과(P6)와 같은 구성 + 페이스 그래프 · 경과 시간 · 시작/종료 시각. */
internal class RecordDetailViewModel(
    private val runId: String,
    runRepository: RunRepository,
    settingsRepository: SettingsRepository,
) : MviViewModel<RecordDetailState, RecordDetailAction, RecordDetailEvent>(RecordDetailState()) {
    init {
        // 편집(R4)에서 돌아오면 바뀐 제목 · 메모가 바로 보이도록 계속 구독한다
        combine(runRepository.observeRun(runId), settingsRepository.observeProfile()) { run, profile ->
            setState {
                if (run == null) {
                    copy(loading = false, missing = true)
                } else {
                    RecordDetailState(
                        loading = false,
                        title = run.title,
                        defaultTitle = RunTitle.defaultOf(run.startedAt, run.zoneId),
                        startedAt = run.startedAt,
                        endedAt = run.endedAt,
                        zoneId = run.zoneId,
                        distanceM = run.distanceM,
                        movingMs = run.movingMs,
                        elapsedMs = run.elapsedMs,
                        averagePaceSecPerKm = Pace.secPerKm(run.movingMs, run.distanceM),
                        calories = run.calories,
                        route = run.routePolyline?.let { PolylineCodec.decode(it, run.routeSegments) }.orEmpty(),
                        splits = SplitRows.of(run.splits),
                        memo = run.memo,
                        distanceUnit = profile.distanceUnit,
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: RecordDetailAction) {
        when (action) {
            RecordDetailAction.EditClicked -> if (!currentState.missing) sendEvent(RecordDetailEvent.OpenEdit(runId))
            RecordDetailAction.BackClicked -> sendEvent(RecordDetailEvent.NavigateBack)
        }
    }
}
