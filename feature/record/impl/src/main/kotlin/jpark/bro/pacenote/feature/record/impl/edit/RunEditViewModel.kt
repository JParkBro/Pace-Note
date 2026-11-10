package jpark.bro.pacenote.feature.record.impl.edit

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.domain.stats.RunTitle
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 기록 편집(R4, product-spec 5-8): 제목(40자) · 메모(500자) 저장, 삭제(soft delete). */
internal class RunEditViewModel(
    private val runId: String,
    private val runRepository: RunRepository,
) : MviViewModel<RunEditState, RunEditAction, RunEditEvent>(RunEditState()) {
    init {
        viewModelScope.launch {
            val run = runRepository.observeRun(runId).first()
            if (run == null) {
                sendEvent(RunEditEvent.Close)
                return@launch
            }
            setState {
                copy(
                    loading = false,
                    title = run.title.orEmpty(),
                    memo = run.memo.orEmpty(),
                    defaultTitle = RunTitle.defaultOf(run.startedAt, run.zoneId),
                )
            }
        }
    }

    override fun onAction(action: RunEditAction) {
        when (action) {
            is RunEditAction.TitleChanged -> setState { copy(title = action.title.take(RunEditState.TITLE_MAX_LENGTH)) }
            is RunEditAction.MemoChanged -> setState { copy(memo = action.memo.take(RunEditState.MEMO_MAX_LENGTH)) }
            RunEditAction.SaveClicked -> save()
            RunEditAction.DeleteClicked -> setState { copy(showDeleteDialog = true) }
            RunEditAction.DeleteDismissed -> setState { copy(showDeleteDialog = false) }
            RunEditAction.DeleteConfirmed -> delete()
            RunEditAction.BackClicked -> sendEvent(RunEditEvent.Close)
        }
    }

    private fun save() {
        if (currentState.loading || currentState.working) return
        setState { copy(working = true) }
        viewModelScope.launch {
            // 빈 제목 · 메모는 Repository가 null로 저장한다
            runRepository
                .updateTitleAndMemo(runId, currentState.title, currentState.memo)
                .onSuccess { sendEvent(RunEditEvent.Close) }
                .onFailure { sendEvent(RunEditEvent.ShowError) }
            setState { copy(working = false) }
        }
    }

    private fun delete() {
        if (currentState.working) return
        setState { copy(showDeleteDialog = false, working = true) }
        viewModelScope.launch {
            runRepository
                .deleteRun(runId)
                .onSuccess { sendEvent(RunEditEvent.Deleted) }
                .onFailure { sendEvent(RunEditEvent.ShowError) }
            setState { copy(working = false) }
        }
    }
}
