package jpark.bro.pacenote.di

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.tracking.di.trackingModule
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.home.impl.di.homeModule
import jpark.bro.pacenote.feature.onboarding.api.OnboardingResultHandler
import jpark.bro.pacenote.feature.onboarding.impl.di.onboardingModule
import jpark.bro.pacenote.feature.record.impl.di.recordModule
import jpark.bro.pacenote.feature.run.api.RunResultHandler
import jpark.bro.pacenote.feature.run.impl.di.runModule
import jpark.bro.pacenote.feature.settings.impl.di.settingsModule
import jpark.bro.pacenote.navigation.AppOnboardingResultHandler
import jpark.bro.pacenote.navigation.AppRunResultHandler
import jpark.bro.pacenote.navigation.AppStartRouter
import jpark.bro.pacenote.navigation.appTopLevelKeys
import jpark.bro.pacenote.update.UpdateViewModel
import jpark.bro.pacenote.widget.di.widgetModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** 모든 모듈의 입구. [buildTypeModule]은 debug · release 소스 세트마다 다르고, 같은 타입은 뒤에 온 정의가 덮어쓴다. */
@OptIn(KoinExperimentalAPI::class)
val appModule =
    module {
        includes(trackingModule, onboardingModule, homeModule, recordModule, runModule, settingsModule, widgetModule)

        single { Navigator(HomeKey, appTopLevelKeys) }
        singleOf(::AppRunResultHandler) bind RunResultHandler::class
        singleOf(::AppStartRouter)
        singleOf(::AppOnboardingResultHandler) bind OnboardingResultHandler::class

        // 인앱 업데이트: UpdateViewModel과 MainActivity가 같은 인스턴스를 쓴다 (docs/run/in-app-update.md 4장)
        single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
        viewModelOf(::UpdateViewModel)

        includes(buildTypeModule)
    }
