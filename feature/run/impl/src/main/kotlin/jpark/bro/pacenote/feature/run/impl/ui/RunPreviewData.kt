package jpark.bro.pacenote.feature.run.impl.ui

import jpark.bro.pacenote.core.model.GpsStatus
import jpark.bro.pacenote.core.model.LiveMetrics
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.tracking.TrackingState
import kotlin.math.sin

// Preview 전용 샘플 (product-spec 9장 진행 중 기록: 3.42km, 20:31, 현재 페이스 5′55″)
@Suppress("MagicNumber")
private val previewRoute =
    List(40) { i -> RoutePoint(lat = 37.52 + i * 0.0004, lng = 126.93 + sin(i / 6.0) * 0.002, segment = 0) }

@Suppress("MagicNumber")
internal fun previewRecording(pause: PauseState = PauseState.RUNNING) =
    TrackingState.Recording(
        runId = "run-preview",
        metrics = LiveMetrics(3_420.0, 1_231_000L, 0L, 355.0, 360.0),
        route = previewRoute,
        pause = pause,
        gps = GpsStatus.GOOD,
    )
