package jpark.bro.pacenote.navigation

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.testing.fake.FakeRunTracker
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.run.api.RunKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStartRouterTest {
    private val navigator = Navigator(HomeKey)
    private val tracker = FakeRunTracker()
    private val router = AppStartRouter(navigator, tracker)

    @Test
    fun `진행 중 기록이 남아 있으면 홈 위에 복구 화면`() =
        runTest {
            tracker.recoverable = RecoverableRun("r1", 0L, 3_420.0, 1_231_000L)

            router.onLaunch()

            assertEquals(listOf(HomeKey, RunKey(recoverRunId = "r1")), navigator.backStack.toList())
        }

    @Test
    fun `남은 기록이 없으면 홈`() =
        runTest {
            router.onLaunch()

            assertEquals(listOf(HomeKey), navigator.backStack.toList())
        }

    @Test
    fun `알림을 누르면 기록 중일 때만 러닝 화면, 이미 열려 있으면 그대로`() {
        router.openRun()
        assertEquals(listOf(HomeKey), navigator.backStack.toList())

        tracker.state.value = FakeRunTracker.recording()
        router.openRun()
        router.openRun()

        assertEquals(listOf(HomeKey, RunKey()), navigator.backStack.toList())
    }

    @Test
    fun `위젯 달리기 시작은 홈 위에 준비 화면`() {
        router.startRun()

        assertEquals(listOf(HomeKey, RunKey()), navigator.backStack.toList())
    }
}
