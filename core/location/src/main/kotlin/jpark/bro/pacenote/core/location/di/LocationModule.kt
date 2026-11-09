package jpark.bro.pacenote.core.location.di

import jpark.bro.pacenote.core.location.FusedLocationSource
import jpark.bro.pacenote.core.location.LocationSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// 디버그 빌드의 GPX 재생 전환은 추적 서비스를 연결할 때(core:tracking) 추가한다
val locationModule =
    module {
        single<LocationSource> { FusedLocationSource(androidContext()) }
    }
