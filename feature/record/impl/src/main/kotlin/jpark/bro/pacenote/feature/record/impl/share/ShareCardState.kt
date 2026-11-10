package jpark.bro.pacenote.feature.record.impl.share

import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RoutePoint

/** 카드 배경 (product-spec 5-6) */
internal enum class ShareBackground {
    /** route-canvas, 1080×1350 */
    DEFAULT,

    /** 사용자가 고른 사진 + image-scrim, 1080×1350 */
    PHOTO,

    /** 흰 선 + 수치만, 1080×1080 투명 PNG (인스타 스토리 스티커) */
    TRANSPARENT,
}

/** 카드에 보여줄 정보. 기본은 거리 · 시간 · 페이스 */
internal enum class ShareField { DISTANCE, TIME, PACE, DATE }

/**
 * @property photoUri 사진 배경으로 고른 이미지 (content URI 문자열)
 * @property working 이미지를 만드는 중 (버튼을 막는다)
 */
internal data class ShareCardState(
    val loading: Boolean = true,
    val startedAt: Long = 0L,
    val zoneId: String = "UTC",
    val distanceM: Double = 0.0,
    val movingMs: Long = 0L,
    val averagePaceSecPerKm: Double? = null,
    val route: List<RoutePoint> = emptyList(),
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val background: ShareBackground = ShareBackground.DEFAULT,
    val photoUri: String? = null,
    val fields: Set<ShareField> = setOf(ShareField.DISTANCE, ShareField.TIME, ShareField.PACE),
    val working: Boolean = false,
)
