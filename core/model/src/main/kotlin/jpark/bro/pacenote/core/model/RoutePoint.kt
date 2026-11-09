package jpark.bro.pacenote.core.model

/** 화면에 그리는 경로의 점. [segment]가 바뀌는 곳은 신호 공백이다. */
data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val segment: Int,
)
