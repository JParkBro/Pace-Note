package jpark.bro.pacenote.navigation

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResult
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResultHandler

/** 온보딩 완료 → 홈만 남긴다 (module-map 4-3) */
class AppOnboardingResultHandler(
    private val navigator: Navigator,
    private val router: AppStartRouter,
) : OnboardingResultHandler {
    override fun handle(result: OnboardingResult) =
        when (result) {
            OnboardingResult.Done -> {
                router.onOnboardingDone()
                navigator.replaceAll(HomeKey)
            }
        }
}
