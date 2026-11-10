package jpark.bro.pacenote.debug

import jpark.bro.pacenote.core.location.LocationSource
import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * debug 빌드 전용: 실제 GPS와 GPX 재생을 바꿔 끼운다 (tracking.md 8-3 "실내에서 전체 흐름 재생").
 * 바꾼 값은 다음 준비(`prepare`)부터 적용된다.
 */
class DebugLocationSource(
    private val device: LocationSource,
    private val replay: (speed: Double) -> LocationSource,
) : LocationSource {
    /** null이면 실제 GPS, 값이 있으면 그 배속으로 GPX 재생 */
    val replaySpeed = MutableStateFlow<Double?>(null)

    override fun locations(): Flow<RawLocation> = current().locations()

    override suspend fun lastKnown(): RawLocation? = current().lastKnown()

    override fun isLocationEnabled(): Boolean = current().isLocationEnabled()

    private fun current(): LocationSource = replaySpeed.value?.let(replay) ?: device
}
