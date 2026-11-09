package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.model.NormalizedPoint
import jpark.bro.pacenote.core.model.ProjectedRoute
import jpark.bro.pacenote.core.model.RoutePoint
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

/** 경로를 비율을 유지한 채 그리는 영역에 맞춘다 (docs/run/tracking.md 10-1). */
object RouteProjector {
    private const val METERS_PER_DEGREE = 111_194.92664455873
    private const val MIN_SPAN_M = 30.0
    private const val CENTER = 0.5f

    /**
     * @param aspectRatio 그리는 영역의 가로 ÷ 세로
     * @param padding 각 변의 여백 (영역 크기 대비 비율, 0 이상 0.5 미만)
     */
    fun project(
        points: List<RoutePoint>,
        aspectRatio: Float,
        padding: Float,
    ): ProjectedRoute {
        if (points.isEmpty()) return ProjectedRoute(emptyList(), isDot = false)
        val bounds = Bounds.of(points)
        return if (max(bounds.widthM, bounds.heightM) < MIN_SPAN_M) {
            ProjectedRoute(listOf(NormalizedPoint(CENTER, CENTER, points.first().segment)), isDot = true)
        } else {
            fit(points, bounds, aspectRatio, padding)
        }
    }

    private fun fit(
        points: List<RoutePoint>,
        bounds: Bounds,
        aspectRatio: Float,
        padding: Float,
    ): ProjectedRoute {
        val available = 1 - 2 * padding
        val scale =
            min(
                if (bounds.widthM > 0) aspectRatio * available / bounds.widthM else Double.MAX_VALUE,
                if (bounds.heightM > 0) available / bounds.heightM else Double.MAX_VALUE,
            )
        val offsetX = (aspectRatio - bounds.widthM * scale) / 2
        val offsetY = (1 - bounds.heightM * scale) / 2
        val projected =
            points.map { point ->
                NormalizedPoint(
                    x = ((bounds.xM(point) * scale + offsetX) / aspectRatio).toFloat(),
                    y = (bounds.yM(point) * scale + offsetY).toFloat(),
                    segment = point.segment,
                )
            }
        return ProjectedRoute(projected, isDot = false)
    }

    private class Bounds(
        private val minLng: Double,
        private val maxLat: Double,
        private val lngScale: Double,
        val widthM: Double,
        val heightM: Double,
    ) {
        fun xM(point: RoutePoint): Double = (point.lng - minLng) * lngScale

        fun yM(point: RoutePoint): Double = (maxLat - point.lat) * METERS_PER_DEGREE

        companion object {
            fun of(points: List<RoutePoint>): Bounds {
                val minLat = points.minOf { it.lat }
                val maxLat = points.maxOf { it.lat }
                val minLng = points.minOf { it.lng }
                val maxLng = points.maxOf { it.lng }
                val lngScale = METERS_PER_DEGREE * cos(Math.toRadians((minLat + maxLat) / 2))
                return Bounds(
                    minLng,
                    maxLat,
                    lngScale,
                    (maxLng - minLng) * lngScale,
                    (maxLat - minLat) * METERS_PER_DEGREE,
                )
            }
        }
    }
}
