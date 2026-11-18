package jpark.bro.pacenote.feature.settings.impl.moreapps

import jpark.bro.pacenote.core.ui.MviViewModel

/**
 * 다른 앱 둘러보기(M11, product-spec 7장): 개발자의 다른 앱 소개 페이지를 웹뷰로 연다.
 * 페이지 주소 · 링크 정책은 화면(Root)과 RunWebView가 맡고, 여기서는 로딩 · 실패 상태만 관리한다.
 */
internal class MoreAppsViewModel : MviViewModel<MoreAppsState, MoreAppsAction, MoreAppsEvent>(MoreAppsState()) {
    override fun onAction(action: MoreAppsAction) {
        when (action) {
            MoreAppsAction.PageLoaded -> setState { copy(isLoading = false, isFailed = false) }
            MoreAppsAction.PageFailed -> setState { copy(isLoading = false, isFailed = true) }
            MoreAppsAction.RetryClicked -> retry()
            is MoreAppsAction.StoreLinkClicked -> sendEvent(MoreAppsEvent.OpenStore(action.url))
            MoreAppsAction.BackClicked -> sendEvent(MoreAppsEvent.NavigateBack)
        }
    }

    private fun retry() {
        setState { copy(isLoading = true, isFailed = false, reloadCount = reloadCount + 1) }
    }
}
