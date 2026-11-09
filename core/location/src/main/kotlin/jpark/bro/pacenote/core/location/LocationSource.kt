package jpark.bro.pacenote.core.location

import jpark.bro.pacenote.core.model.RawLocation
import kotlinx.coroutines.flow.Flow

/**
 * 위치 제공자 (tracking.md 2장). 실제는 [FusedLocationSource], 테스트 · 디버그 재생은 [GpxReplaySource].
 * 요청 값은 기록 중과 준비(P1-c)가 같다: 높은 정확도, 1초 간격, 최소 이동 거리 0m (tracking.md 4-1).
 */
interface LocationSource {
    /**
     * 구독하는 동안 점을 보낸다. 구독을 끊으면 위치 요청을 해제한다.
     * 정밀 위치 권한은 호출하는 쪽이 먼저 확인한다. 권한이 없으면 [SecurityException]으로 끝난다.
     */
    fun locations(): Flow<RawLocation>

    /** 캐시된 마지막 위치. 없거나 권한이 없으면 null */
    suspend fun lastKnown(): RawLocation?

    /** 기기 위치 설정이 켜져 있는지 */
    fun isLocationEnabled(): Boolean
}
