package jpark.bro.pacenote.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import jpark.bro.pacenote.R
import jpark.bro.pacenote.core.designsystem.component.RunBottomBar
import jpark.bro.pacenote.core.designsystem.component.RunTab
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.record.api.RecordKey
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 하단 탭 (product-spec 4-1). 내 정보 탭은 설정 화면(7단계)과 함께 추가한다. */
private enum class AppTab(
    val key: NavKey,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    HOME(HomeKey, R.string.app_tab_home, DesignR.drawable.ic_home),
    RECORD(RecordKey, R.string.app_tab_record, DesignR.drawable.ic_bar_chart),
}

/** [Navigator]에 넘길 하단 탭 첫 화면들 */
val appTopLevelKeys: Set<NavKey> = AppTab.entries.map { it.key }.toSet()

/**
 * 앱 전체 화면. 각 기능의 Koin 모듈이 `navigation<Key>`로 등록한 화면을 그린다 (ANDROID_ARCHITECTURE 5-5).
 * 하단 탭바는 탭의 첫 화면에서만 보인다. 러닝 흐름 · 상세 화면은 탭바 없이 전체 화면이다.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavDisplay(navigator: Navigator = koinInject()) {
    val tabs = AppTab.entries
    Scaffold(
        bottomBar = {
            if (navigator.isAtTabRoot) {
                RunBottomBar(
                    tabs = tabs.map { RunTab(stringResource(it.label), it.icon) },
                    selectedIndex = tabs.indexOfFirst { it.key == navigator.currentTab },
                    onSelect = { navigator.goTo(tabs[it].key) },
                )
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            // ViewModelStore 데코레이터를 빼면 백스택에서 빠진 화면의 ViewModel이 Activity 수명만큼 남는다
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider = koinEntryProvider(),
        )
    }
}
