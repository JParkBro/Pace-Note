package jpark.bro.pacenote.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.core.tracking.di.trackingModule
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.run.api.RunResultHandler
import jpark.bro.pacenote.navigation.AppRunResultHandler
import jpark.bro.pacenote.navigation.TemporaryHome
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/** 모든 모듈의 입구. [buildTypeModule]은 debug · release 소스 세트마다 다르고, 같은 타입은 뒤에 온 정의가 덮어쓴다. */
@OptIn(KoinExperimentalAPI::class)
val appModule =
    module {
        includes(trackingModule)

        single { Navigator(HomeKey) }
        singleOf(::AppRunResultHandler) bind RunResultHandler::class

        // 홈 화면(#8) 전까지 자리표시자
        navigation<HomeKey> { TemporaryHome { BuildTypeHomeExtras() } }

        includes(buildTypeModule)
    }
