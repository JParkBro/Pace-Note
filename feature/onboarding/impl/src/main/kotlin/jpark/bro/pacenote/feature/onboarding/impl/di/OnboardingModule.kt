package jpark.bro.pacenote.feature.onboarding.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.onboarding.api.OnboardingKey
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResult
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.pacenote.feature.onboarding.impl.basic.BasicInfoRoot
import jpark.bro.pacenote.feature.onboarding.impl.basic.BasicInfoViewModel
import jpark.bro.pacenote.feature.onboarding.impl.goal.FirstGoalKey
import jpark.bro.pacenote.feature.onboarding.impl.goal.FirstGoalRoot
import jpark.bro.pacenote.feature.onboarding.impl.goal.FirstGoalViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val onboardingModule =
    module {
        viewModelOf(::BasicInfoViewModel)
        viewModelOf(::FirstGoalViewModel)

        // 지금은 O2(기본 정보)부터. O0 · O1은 외부 설정 단계(module-map 9-3)에서 앞에 붙인다
        navigation<OnboardingKey> {
            val navigator = get<Navigator>()
            BasicInfoRoot(onNext = { navigator.goTo(FirstGoalKey) }, viewModel = koinViewModel())
        }

        navigation<FirstGoalKey> {
            val navigator = get<Navigator>()
            val resultHandler = get<OnboardingResultHandler>()
            FirstGoalRoot(
                onBack = navigator::goBack,
                onDone = { resultHandler.handle(OnboardingResult.Done) },
                viewModel = koinViewModel(),
            )
        }
    }
