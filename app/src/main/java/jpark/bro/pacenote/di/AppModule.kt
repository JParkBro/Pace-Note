package jpark.bro.pacenote.di

import jpark.bro.pacenote.core.tracking.di.trackingModule
import org.koin.dsl.module

/** 모든 모듈의 입구. [buildTypeModule]은 debug · release 소스 세트마다 다르고, 같은 타입은 뒤에 온 정의가 덮어쓴다. */
val appModule =
    module {
        includes(trackingModule, buildTypeModule)
    }
