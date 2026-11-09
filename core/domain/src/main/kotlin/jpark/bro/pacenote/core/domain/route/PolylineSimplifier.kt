package jpark.bro.pacenote.core.domain.route

import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.model.SimplifiedRoute
import kotlin.math.cos
import kotlin.math.hypot

/** segment별 Douglas-Peucker 단순화 (docs/run/tracking.md 10-3). */
object PolylineSimplifier {
    private const val INITIAL_TOLERANCE_M = 5.0
    private const val TOLERANCE_GROWTH = 1.5
    private const val MAX_POINTS = 500
    private const val METERS_PER_DEGREE = 111_194.92664455873

    fun simplify(points: List<RoutePoint>): SimplifiedRoute {
        val segments = points.groupBy { it.segment }.values.toList()
        var toleranceM = INITIAL_TOLERANCE_M
        var result = segments.map { douglasPeucker(it, toleranceM) }
        while (result.sumOf { it.size } > MAX_POINTS && toleranceM < Double.MAX_VALUE) {
            toleranceM *= TOLERANCE_GROWTH
            result = segments.map { douglasPeucker(it, toleranceM) }
        }
        return SimplifiedRoute(result.flatten(), result.map { it.size })
    }

    private fun douglasPeucker(
        points: List<RoutePoint>,
        toleranceM: Double,
    ): List<RoutePoint> {
        if (points.size <= 2) return points
        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.lastIndex] = true
        val ranges = ArrayDeque(listOf(0 to points.lastIndex))
        while (ranges.isNotEmpty()) {
            val (start, end) = ranges.removeLast()
            val farthest = (start + 1 until end).maxByOrNull { distanceToLineM(points[it], points[start], points[end]) }
            if (farthest != null && distanceToLineM(points[farthest], points[start], points[end]) > toleranceM) {
                keep[farthest] = true
                ranges.addLast(start to farthest)
                ranges.addLast(farthest to end)
            }
        }
        return points.filterIndexed { index, _ -> keep[index] }
    }

    /** 짧은 구간이라 위도 보정한 평면으로 계산한다. */
    private fun distanceToLineM(
        point: RoutePoint,
        start: RoutePoint,
        end: RoutePoint,
    ): Double {
        val lngScale = METERS_PER_DEGREE * cos(Math.toRadians(start.lat))
        val px = (point.lng - start.lng) * lngScale
        val py = (point.lat - start.lat) * METERS_PER_DEGREE
        val ex = (end.lng - start.lng) * lngScale
        val ey = (end.lat - start.lat) * METERS_PER_DEGREE
        val lengthSquared = ex * ex + ey * ey
        val ratio = if (lengthSquared == 0.0) 0.0 else ((px * ex + py * ey) / lengthSquared).coerceIn(0.0, 1.0)
        return hypot(px - ex * ratio, py - ey * ratio)
    }
}
