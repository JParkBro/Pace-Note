package jpark.bro.pacenote.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.tracking.di.trackingModule
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.home.impl.di.homeModule
import jpark.bro.pacenote.feature.record.impl.di.recordModule
import jpark.bro.pacenote.feature.run.api.RunResultHandler
import jpark.bro.pacenote.feature.run.impl.di.runModule
import jpark.bro.pacenote.navigation.AppRunResultHandler
import jpark.bro.pacenote.navigation.AppStartRouter
import jpark.bro.pacenote.navigation.appTopLevelKeys
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** 모든 모듈의 입구. [buildTypeModule]은 debug · release 소스 세트마다 다르고, 같은 타입은 뒤에 온 정의가 덮어쓴다. */
@OptIn(KoinExperimentalAPI::class)
val appModule =
    module {
        includes(trackingModule, homeModule, recordModule, runModule)

        single { Navigator(HomeKey, appTopLevelKeys) }
        singleOf(::AppRunResultHandler) bind RunResultHandler::class
        singleOf(::AppStartRouter)

        includes(buildTypeModule)
    }
