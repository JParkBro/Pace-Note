package jpark.bro.pacenote.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import jpark.bro.pacenote.core.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/** 앱 전체 화면. 각 기능의 Koin 모듈이 `navigation<Key>`로 등록한 화면을 그린다 (ANDROID_ARCHITECTURE 5-5). */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavDisplay(navigator: Navigator = koinInject()) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::goBack,
        // ViewModelStore 데코레이터를 빼면 백스택에서 빠진 화면의 ViewModel이 Activity 수명만큼 남는다
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = koinEntryProvider(),
    )
}
