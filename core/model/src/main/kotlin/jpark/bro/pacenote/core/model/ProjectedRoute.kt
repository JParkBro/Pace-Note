package jpark.bro.pacenote.core.model

/** 그리는 영역 기준 0~1 좌표의 점. [y]는 아래로 커진다. */
data class NormalizedPoint(
    val x: Float,
    val y: Float,
    val segment: Int,
)

/** @property isDot 경로가 아주 짧아 영역 중앙에 점 하나로 표시한다 */
data class ProjectedRoute(
    val points: List<NormalizedPoint>,
    val isDot: Boolean,
)
