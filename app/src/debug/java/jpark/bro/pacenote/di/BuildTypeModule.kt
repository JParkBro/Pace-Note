package jpark.bro.pacenote.di

import jpark.bro.pacenote.core.location.FusedLocationSource
import jpark.bro.pacenote.core.location.GpxParser
import jpark.bro.pacenote.core.location.GpxReplaySource
import jpark.bro.pacenote.core.location.LocationSource
import jpark.bro.pacenote.debug.DebugLocationSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val REPLAY_GPX = "gpx/long_60min.gpx"

/** debug 빌드: 위치 소스를 GPX 재생으로 바꿀 수 있게 덮어쓴다. */
val buildTypeModule =
    module {
        single {
            val context = androidContext()
            val points by lazy { context.assets.open(REPLAY_GPX).use(GpxParser::parse) }
            DebugLocationSource(FusedLocationSource(context)) { speed ->
                GpxReplaySource(points, elapsedClock = get(), clock = get(), speed = speed)
            }
        }
        single<LocationSource> { get<DebugLocationSource>() }
    }
