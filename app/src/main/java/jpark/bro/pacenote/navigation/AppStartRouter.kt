package jpark.bro.pacenote.navigation

import android.content.Intent
import jpark.bro.pacenote.core.navigation.AppIntents
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.run.api.RunKey

/**
 * 앱 시작 · 알림 · 위젯으로 들어왔을 때의 목적지 (module-map 4-3).
 * 약관 동의 전 → 온보딩 분기는 온보딩 화면을 만들 때 추가한다.
 */
class AppStartRouter(
    private val navigator: Navigator,
    private val tracker: RunTracker,
) {
    /** 처음 실행(프로세스 시작): 진행 중 기록이 남아 있으면 [홈, 복구(P8)] */
    suspend fun onLaunch() {
        if (tracker.state.value != TrackingState.Idle) return
        tracker.recoverableRun()?.let { navigator.replaceAll(HomeKey, RunKey(recoverRunId = it.runId)) }
    }

    fun onIntent(intent: Intent?) {
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
