package jpark.bro.pacenote.di

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import jpark.bro.pacenote.R
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton
import jpark.bro.pacenote.core.location.FusedLocationSource
import jpark.bro.pacenote.core.location.GpxParser
import jpark.bro.pacenote.core.location.GpxReplaySource
import jpark.bro.pacenote.core.location.LocationSource
import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.debug.DebugLocationSource
import jpark.bro.pacenote.debug.TrackingDebugScreen
import kotlinx.serialization.Serializable
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

private const val REPLAY_GPX = "gpx/long_60min.gpx"

/** debug 빌드 전용 추적 테스트 화면 (tracking.md 8-3) */
@Serializable
data object DebugTrackingKey : NavKey

/** debug 빌드: 위치 소스를 GPX 재생으로 바꿀 수 있게 덮어쓰고, 추적 테스트 화면을 등록한다. */
@OptIn(KoinExperimentalAPI::class)
val buildTypeModule =
    module {
        navigation<DebugTrackingKey> { TrackingDebugScreen() }

        single {
            val context = androidContext()
            val points by lazy { context.assets.open(REPLAY_GPX).use(GpxParser::parse) }
            DebugLocationSource(FusedLocationSource(context)) { speed ->
                GpxReplaySource(points, elapsedClock = get(), clock = get(), speed = speed)
            }
        }
        single<LocationSource> { get<DebugLocationSource>() }
    }

/** debug 빌드의 임시 홈: 추적 테스트 화면으로 이동 */
@Composable
fun ColumnScope.BuildTypeHomeExtras(navigator: Navigator = koinInject()) {
    RunSecondaryButton(
        text = stringResource(R.string.debug_tracking_open),
        onClick = { navigator.goTo(DebugTrackingKey) },
    )
}
