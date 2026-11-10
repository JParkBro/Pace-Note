package jpark.bro.pacenote.navigation

import android.content.Intent
import jpark.bro.pacenote.core.data.repository.OnboardingRepository
import jpark.bro.pacenote.core.navigation.AppIntents
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.onboarding.api.OnboardingKey
import jpark.bro.pacenote.feature.run.api.RunKey
import kotlinx.coroutines.flow.first

/**
 * 앱 시작 · 알림 · 위젯으로 들어왔을 때의 목적지 (module-map 4-3).
 */
class AppStartRouter(
    private val navigator: Navigator,
    private val tracker: RunTracker,
    private val onboardingRepository: OnboardingRepository,
) {
    /** 온보딩을 마치기 전에는 알림 · 위젯으로 들어와도 온보딩을 이어간다 */
    private var onboarding = false

    /**
     * 처음 실행(프로세스 시작): 온보딩 전이면 온보딩, 진행 중 기록이 남아 있으면 [홈, 복구(P8)].
     */
    suspend fun onLaunch() {
        if (!onboardingRepository.observeOnboardingCompleted().first()) {
            onboarding = true
            navigator.replaceAll(OnboardingKey)
            return
        }
        if (tracker.state.value != TrackingState.Idle) return
        tracker.recoverableRun()?.let { navigator.replaceAll(HomeKey, RunKey(recoverRunId = it.runId)) }
    }

    /** 온보딩이 끝났다 (`OnboardingResult.Done`) */
    fun onOnboardingDone() {
        onboarding = false
    }

    fun onIntent(intent: Intent?) {
        if (onboarding) return
        when {
            intent?.getBooleanExtra(AppIntents.EXTRA_OPEN_RUN, false) == true -> openRun()
            intent?.getBooleanExtra(AppIntents.EXTRA_START_RUN, false) == true -> startRun()
        }
    }

    /** 진행 알림 탭: 기록 중일 때만 러닝 화면으로. 이미 러닝 화면이면 그대로 */
    fun openRun() {
        if (tracker.state.value !is TrackingState.Recording) return
        if (navigator.backStack.lastOrNull() is RunKey) return
        navigator.replaceAll(HomeKey, RunKey())
    }

    /** 위젯 "달리기 시작": 준비(P1)부터 */
    fun startRun() {
        if (navigator.backStack.lastOrNull() is RunKey) return
        navigator.replaceAll(HomeKey, RunKey())
    }
}
