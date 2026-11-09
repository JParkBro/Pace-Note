package jpark.bro.pacenote.core.model

/** @property segmentCounts segment별 점 개수. `run.route_segments`에 저장한다 */
data class SimplifiedRoute(
    val points: List<RoutePoint>,
    val segmentCounts: List<Int>,
)
