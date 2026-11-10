package jpark.bro.pacenote.feature.record.impl.share

import androidx.lifecycle.viewModelScope
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.domain.route.PolylineCodec
import jpark.bro.pacenote.core.domain.tracking.Pace
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 공유 카드(P7, product-spec 5-6). 배경 · 표시 정보를 고르고, 이미지 만들기는 Root가 맡는다
 * (ViewModel은 Context를 모르므로 저장 · 공유는 Event로 요청하고 결과만 받는다).
 */
internal class ShareCardViewModel(
    private val runId: String,
    private val runRepository: RunRepository,
    private val settingsRepository: SettingsRepository,
) : MviViewModel<ShareCardState, ShareCardAction, ShareCardEvent>(ShareCardState()) {
    init {
        viewModelScope.launch { load() }
    }

    override fun onAction(action: ShareCardAction) {
        when (action) {
            is ShareCardAction.BackgroundSelected -> onBackgroundSelected(action.background)
            is ShareCardAction.PhotoPicked -> onPhotoPicked(action.uri)
            is ShareCardAction.FieldToggled -> onFieldToggled(action.field)
            ShareCardAction.SaveClicked -> export(ShareCardEvent.ExportToGallery)
            ShareCardAction.ShareClicked -> export(ShareCardEvent.ExportToShareSheet)
            is ShareCardAction.ExportFinished -> onExportFinished(action)
            ShareCardAction.BackClicked -> sendEvent(ShareCardEvent.NavigateBack)
        }
    }

    private suspend fun load() {
        val run = runRepository.observeRun(runId).first()
        if (run == null) {
            sendEvent(ShareCardEvent.NavigateBack)
            return
        }
        val unit = settingsRepository.observeProfile().first().distanceUnit
        setState {
            copy(
                loading = false,
                startedAt = run.startedAt,
                zoneId = run.zoneId,
                distanceM = run.distanceM,
                movingMs = run.movingMs,
                averagePaceSecPerKm = Pace.secPerKm(run.movingMs, run.distanceM),
                route = run.routePolyline?.let { PolylineCodec.decode(it, run.routeSegments) }.orEmpty(),
                distanceUnit = unit,
            )
        }
    }

    private fun onBackgroundSelected(background: ShareBackground) {
        if (background == ShareBackground.PHOTO) {
            // 사진을 고른 뒤에야 사진 배경으로 바꾼다
            sendEvent(ShareCardEvent.OpenPhotoPicker)
        } else {
            setState { copy(background = background) }
        }
    }

    private fun onPhotoPicked(uri: String?) {
        if (uri != null) setState { copy(background = ShareBackground.PHOTO, photoUri = uri) }
    }

    private fun onFieldToggled(field: ShareField) {
        setState { copy(fields = if (field in fields) fields - field else fields + field) }
    }

    private fun export(event: ShareCardEvent) {
        if (currentState.loading || currentState.working) return
        setState { copy(working = true) }
        sendEvent(event)
    }

    private fun onExportFinished(result: ShareCardAction.ExportFinished) {
        setState { copy(working = false) }
        when {
            !result.success -> sendEvent(ShareCardEvent.ShowError)
            result.saved -> sendEvent(ShareCardEvent.ShowSaved)
        }
    }
}
