package jpark.bro.pacenote.core.tracking.di

import jpark.bro.pacenote.core.common.ElapsedClock
import jpark.bro.pacenote.core.data.di.dataModule
import jpark.bro.pacenote.core.location.di.locationModule
import jpark.bro.pacenote.core.tracking.RunRecorder
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.SystemElapsedClock
import jpark.bro.pacenote.core.tracking.TrackingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val trackingModule =
    module {
        includes(dataModule, locationModule)

        single<ElapsedClock> { SystemElapsedClock }
        singleOf(::RunRecorder)
        // 화면이 사라져도 추적이 이어지도록 앱 범위 스코프에서 돈다
        single<RunTracker> {
            TrackingEngine(
                locationSource = get(),
                recorder = get(),
                authRepository = get(),
                settingsRepository = get(),
                elapsedClock = get(),
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            )
        }
    }
