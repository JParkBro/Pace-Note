package jpark.bro.pacenote.feature.settings.impl.moreapps

/** @property reloadCount 다시 시도할 때마다 1 올린다. 웹뷰가 이 값이 바뀌면 같은 인스턴스로 다시 불러온다 */
internal data class MoreAppsState(
    val isLoading: Boolean = true,
    val isFailed: Boolean = false,
    val reloadCount: Int = 0,
)
