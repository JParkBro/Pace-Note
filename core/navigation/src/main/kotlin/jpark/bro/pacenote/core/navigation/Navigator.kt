package jpark.bro.pacenote.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * 앱 하나의 백스택 (ANDROID_ARCHITECTURE 5-3). 하단 탭([topLevelKeys])마다 백스택을 따로 둔다.
 *
 * - [backStack] = 시작 탭 스택 + (현재 탭이 시작 탭이 아니면) 현재 탭 스택. `NavDisplay`가 이것을 그린다.
 * - `goTo(탭 키)`는 탭 전환. 다른 탭의 화면 위치(어디까지 들어갔는지)는 유지된다.
 * - 탭의 첫 화면에서 뒤로 가면 시작 탭으로 돌아간다.
 *
 * @param startKey 시작 탭의 첫 화면
 * @param topLevelKeys 하단 탭의 첫 화면들. [startKey]는 항상 포함된다
 */
class Navigator(
    private val startKey: NavKey,
    topLevelKeys: Set<NavKey> = emptySet(),
) {
    /** 하단 탭의 첫 화면들 (탭바 순서) */
    val topLevelKeys: Set<NavKey> = linkedSetOf(startKey) + topLevelKeys

    private val stacks: Map<NavKey, SnapshotStateList<NavKey>> =
        this.topLevelKeys.associateWith { mutableStateListOf(it) }

    /** 지금 보고 있는 탭의 첫 화면 */
    var currentTab: NavKey by mutableStateOf(startKey)
        private set

    val backStack: List<NavKey>
        get() {
            val start = stack(startKey)
            return if (currentTab == startKey) start.toList() else start + stack(currentTab)
        }

    /** 지금 맨 위 화면이 탭의 첫 화면인지 (하단 탭바를 보여줄지) */
    val isAtTabRoot: Boolean
        get() = stack(currentTab).size == 1

    /** 탭 키면 그 탭으로 바꾸고, 아니면 현재 탭에 쌓는다. */
    fun goTo(key: NavKey) {
        if (key in topLevelKeys) {
            currentTab = key
        } else {
            stack(currentTab).add(key)
        }
    }

    /** 탭 안에서 한 단계 뒤로. 탭의 첫 화면이면 시작 탭으로, 시작 탭의 첫 화면이면 아무것도 하지 않는다 (시스템이 앱을 닫는다). */
    fun goBack() {
        val current = stack(currentTab)
        when {
            current.size > 1 -> current.removeAt(current.lastIndex)
            currentTab != startKey -> currentTab = startKey
        }
    }

    /**
     * 모든 탭을 첫 화면으로 되돌리고 [keys]로 바꾼다. 예: 결과 "완료" → 홈, 앱 시작 → [홈, 러닝 복구].
     * 첫 키가 탭 키면 그 탭에서, 아니면 시작 탭 위에 쌓는다.
     */
    fun replaceAll(vararg keys: NavKey) {
        require(keys.isNotEmpty()) { "바꿀 화면이 없음" }
        stacks.forEach { (root, stack) ->
            stack.clear()
            stack.add(root)
        }
        val first = keys.first()
        currentTab = if (first in topLevelKeys) first else startKey
        keys.drop(if (first in topLevelKeys) 1 else 0).forEach { stack(currentTab).add(it) }
    }

    /** 현재 화면을 다른 화면으로 바꾼다. 탭의 첫 화면은 바꾸지 않고 그 위에 쌓는다. */
    fun replaceTop(key: NavKey) {
        val current = stack(currentTab)
        if (current.size > 1) current.removeAt(current.lastIndex)
        current.add(key)
    }

    private fun stack(tab: NavKey): SnapshotStateList<NavKey> = checkNotNull(stacks[tab])
}
