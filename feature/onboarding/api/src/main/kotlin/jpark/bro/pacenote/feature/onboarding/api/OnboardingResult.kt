package jpark.bro.pacenote.feature.onboarding.api

/** 온보딩이 끝났을 때 알리는 결과. 다음 목적지는 app이 정한다 (module-map 4-3). */
sealed interface OnboardingResult {
    data object Done : OnboardingResult
}

/** 구현은 app에 있다. */
fun interface OnboardingResultHandler {
    fun handle(result: OnboardingResult)
}
