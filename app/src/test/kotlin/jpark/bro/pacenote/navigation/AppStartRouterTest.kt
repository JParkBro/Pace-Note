package jpark.bro.pacenote.navigation

import android.app.Application
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import jpark.bro.pacenote.core.navigation.AppIntents
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.testing.fake.FakeOnboardingRepository
import jpark.bro.pacenote.core.testing.fake.FakeRunTracker
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.onboarding.api.OnboardingKey
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResult
import jpark.bro.pacenote.feature.run.api.RunKey
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Intent를 쓰는 테스트가 있어 Robolectric으로 돌린다. 앱 Application(Koin 시작)은 띄우지 않는다
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppStartRouterTest {
    private val navigator = Navigator(HomeKey)
    private val tracker = FakeRunTracker()
    private val onboarding = FakeOnboardingRepository().apply { onboardingCompleted.value = true }
    private val router = AppStartRouter(navigator, tracker, onboarding)

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

    @Test
    fun `온보딩 전이면 온보딩부터, 그동안 위젯 진입은 무시`() =
        runTest {
            onboarding.onboardingCompleted.value = false

            router.onLaunch()
            router.onIntent(Intent().putExtra(AppIntents.EXTRA_START_RUN, true))

            assertEquals(OnboardingKey, navigator.backStack.last())
        }

    @Test
    fun `온보딩을 마치면 홈만 남음`() =
        runTest {
            onboarding.onboardingCompleted.value = false
            router.onLaunch()

            AppOnboardingResultHandler(navigator, router).handle(OnboardingResult.Done)

            assertEquals(listOf(HomeKey), navigator.backStack.toList())
        }
}
