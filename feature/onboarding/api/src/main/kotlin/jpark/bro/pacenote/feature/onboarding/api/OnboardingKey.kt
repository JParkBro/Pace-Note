package jpark.bro.pacenote.feature.onboarding.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 온보딩 시작. 지금은 로컬 단계(O2 기본 정보 → O3 첫 목표)부터 시작하고,
 * O0 시작 · O1 약관 동의는 외부 설정 단계(module-map 9-3)에서 앞에 붙인다.
 */
@Serializable
data object OnboardingKey : NavKey
