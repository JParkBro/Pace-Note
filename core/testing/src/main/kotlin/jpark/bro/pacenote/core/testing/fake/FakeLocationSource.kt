package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.location.LocationSource
import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/** [emit]으로 점을 직접 보낸다. [isSubscribed]로 위치 요청이 켜져 있는지 확인한다. */
class FakeLocationSource : LocationSource {
    private val points = MutableSharedFlow<RawLocation>(extraBufferCapacity = Int.MAX_VALUE)
    val isSubscribed = MutableStateFlow(false)
    var enabled = true
    var last: RawLocation? = null

    fun emit(point: RawLocation) {
        check(points.tryEmit(point))
    }

    override fun locations(): Flow<RawLocation> =
        points
            .onStart { isSubscribed.value = true }
            .onCompletion { isSubscribed.value = false }

    override suspend fun lastKnown(): RawLocation? = last

    override fun isLocationEnabled(): Boolean = enabled
}
