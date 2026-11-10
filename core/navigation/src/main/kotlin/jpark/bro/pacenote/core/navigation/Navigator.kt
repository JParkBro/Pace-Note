package jpark.bro.pacenote.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * 앱 하나의 백스택 (ANDROID_ARCHITECTURE 5-3). `NavDisplay`가 [backStack]을 그린다.
 * 하단 탭별 백스택은 탭 화면(홈 · 기록 · 내 정보)을 만들 때 추가한다.
 */
class Navigator(
    startKey: NavKey,
) {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(startKey)

    fun goTo(key: NavKey) {
        backStack.add(key)
    }

    /** 첫 화면에서는 아무것도 하지 않는다 (시스템이 앱을 닫는다). */
    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    /** 이전 화면을 모두 지우고 [keys]로 바꾼다. 예: 결과 "완료" → 홈, 앱 시작 → [홈, 러닝 복구] */
    fun replaceAll(vararg keys: NavKey) {
        require(keys.isNotEmpty()) { "바꿀 화면이 없음" }
        backStack.clear()
        backStack.addAll(keys)
    }

    /** 현재 화면을 다른 화면으로 바꾼다. */
    fun replaceTop(key: NavKey) {
        backStack.removeAt(backStack.lastIndex)
        backStack.add(key)
    }
}
