package jpark.bro.pacenote.core.tracking

import android.content.Context
import jpark.bro.pacenote.core.tracking.service.RunTrackingService

/**
 * 기록을 시작하면 위치 타입 포그라운드 서비스를 띄운다 (tracking.md 7-2). 나머지는 [TrackingEngine]이 한다.
 * 서비스는 상태가 Idle로 돌아가면 스스로 내려간다.
 */
internal class ForegroundRunTracker(
    private val engine: TrackingEngine,
    private val context: Context,
) : RunTracker by engine {
    override suspend fun start(recoverRunId: String?): Result<String> =
        engine.start(recoverRunId).onSuccess { RunTrackingService.start(context) }
}
