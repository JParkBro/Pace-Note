package jpark.bro.pacenote.feature.settings.impl.moreapps

import app.cash.turbine.test
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoreAppsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `처음에는 로딩 중`() {
        assertEquals(
            MoreAppsState(isLoading = true, isFailed = false, reloadCount = 0),
            MoreAppsViewModel().state.value,
        )
    }

    @Test
    fun `페이지를 다 불러오면 로딩이 끝나고 페이지를 보여준다`() {
        val vm = MoreAppsViewModel()

        vm.onAction(MoreAppsAction.PageLoaded)

        assertEquals(MoreAppsState(isLoading = false, isFailed = false), vm.state.value)
    }

    @Test
    fun `페이지를 못 불러오면 실패 상태`() {
        val vm = MoreAppsViewModel()

        vm.onAction(MoreAppsAction.PageFailed)

        assertEquals(MoreAppsState(isLoading = false, isFailed = true), vm.state.value)
    }

    @Test
    fun `다시 시도하면 로딩으로 돌아가고 다시 불러오기 카운터가 오른다`() {
        val vm = MoreAppsViewModel()
        vm.onAction(MoreAppsAction.PageFailed)

        vm.onAction(MoreAppsAction.RetryClicked)

        assertEquals(MoreAppsState(isLoading = true, isFailed = false, reloadCount = 1), vm.state.value)
    }

    @Test
    fun `다시 시도 뒤 불러오면 실패가 풀린다`() {
        val vm = MoreAppsViewModel()
        vm.onAction(MoreAppsAction.PageFailed)
        vm.onAction(MoreAppsAction.RetryClicked)

        vm.onAction(MoreAppsAction.PageLoaded)

        assertEquals(MoreAppsState(isLoading = false, isFailed = false, reloadCount = 1), vm.state.value)
    }

    @Test
    fun `스토어 링크를 누르면 스토어 열기 이벤트`() =
        runTest {
            val vm = MoreAppsViewModel()
            val url = "https://play.google.com/store/apps/details?id=jpark.bro.other"

            vm.event.test {
                vm.onAction(MoreAppsAction.StoreLinkClicked(url))
                assertEquals(MoreAppsEvent.OpenStore(url), awaitItem())
            }
        }

    @Test
    fun `뒤로를 누르면 이전 화면으로`() =
        runTest {
            val vm = MoreAppsViewModel()

            vm.event.test {
                vm.onAction(MoreAppsAction.BackClicked)
                assertEquals(MoreAppsEvent.NavigateBack, awaitItem())
            }
        }
}
