package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppColors
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface
import jpark.bro.pacenote.core.model.NormalizedPoint
import jpark.bro.pacenote.core.model.ProjectedRoute

private const val THUMBNAIL_STROKE_RATIO = 0.03f
private const val MARKER_RADIUS_RATIO = 1.5f
private const val END_RING_STROKE_RATIO = 0.6f
private const val DASH_RATIO = 2.5f

enum class RouteCanvasStyle {
    /** 결과 · 상세 경로 카드 */
    CARD,

    /** 목록 썸네일: 크기에 비례한 선, 표시 점 없음 */
    THUMBNAIL,

    /** 러닝 중: 끝점이 현재 위치 점 */
    LIVE,

    /** 공유 카드: 배경은 카드 쪽이 그린다 */
    SHARE,
}

/**
 * 경로 그리기 (theme-guide.md 5.7, tracking.md 10-2).
 * [route]는 이 컴포넌트의 가로세로 비율로 투영한 값이어야 한다.
 */
@Composable
fun RouteCanvas(
    route: ProjectedRoute,
    style: RouteCanvasStyle,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = AppThemeTokens.colors
    val background =
        when (style) {
            RouteCanvasStyle.CARD, RouteCanvasStyle.LIVE -> AppShapes.lg to colors.routeCanvas
            RouteCanvasStyle.THUMBNAIL -> AppShapes.md to colors.routeCanvas
            RouteCanvasStyle.SHARE -> null
        }
    Spacer(
        modifier =
            modifier
                .then(
                    if (contentDescription !=
                        null
                    ) {
                        Modifier.semantics { this.contentDescription = contentDescription }
                    } else {
                        Modifier
                    },
                ).routeBackground(background)
                .drawWithCache {
                    val strokePx =
                        when (style) {
                            RouteCanvasStyle.THUMBNAIL -> size.minDimension * THUMBNAIL_STROKE_RATIO
                            RouteCanvasStyle.SHARE -> 6.dp.toPx()
                            RouteCanvasStyle.CARD, RouteCanvasStyle.LIVE -> 4.dp.toPx()
                        }
                    val geometry = RouteGeometry.of(route, size)
                    onDrawBehind { drawRoute(geometry, route, style, strokePx, colors) }
                },
    )
}

private fun Modifier.routeBackground(background: Pair<Shape, Color>?): Modifier =
    if (background == null) this else clip(background.first).background(background.second)

private class RouteGeometry(
    val line: Path,
    val gaps: Path,
    val first: Offset?,
    val last: Offset?,
) {
    companion object {
        fun of(
            route: ProjectedRoute,
            size: Size,
        ): RouteGeometry {
            val line = Path()
            val gaps = Path()
            var previous: NormalizedPoint? = null
            route.points.forEach { point ->
                val offset = point.toOffset(size)
                val last = previous
                when {
                    last == null -> {
                        line.moveTo(offset.x, offset.y)
                    }

                    last.segment == point.segment -> {
                        line.lineTo(offset.x, offset.y)
                    }

                    else -> {
                        val from = last.toOffset(size)
                        gaps.moveTo(from.x, from.y)
                        gaps.lineTo(offset.x, offset.y)
                        line.moveTo(offset.x, offset.y)
                    }
                }
                previous = point
            }
            return RouteGeometry(
                line,
                gaps,
                route.points.firstOrNull()?.toOffset(size),
                route.points.lastOrNull()?.toOffset(size),
            )
        }

        private fun NormalizedPoint.toOffset(size: Size) = Offset(x * size.width, y * size.height)
    }
}

private fun DrawScope.drawRoute(
    geometry: RouteGeometry,
    route: ProjectedRoute,
    style: RouteCanvasStyle,
    strokePx: Float,
    colors: AppColors,
) {
    val radius = strokePx * MARKER_RADIUS_RATIO
    val first = geometry.first ?: return
    if (route.isDot) {
        drawCircle(colors.routeStart, radius, first)
        return
    }
    drawPath(
        path = geometry.gaps,
        color = colors.routeLineMuted,
        style =
            Stroke(
                width = strokePx,
                cap = StrokeCap.Butt,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokePx * DASH_RATIO, strokePx * DASH_RATIO)),
            ),
    )
    drawPath(
        path = geometry.line,
        color = colors.routeLine,
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    if (style != RouteCanvasStyle.THUMBNAIL) drawMarkers(first, geometry.last ?: first, style, strokePx, colors)
}

private fun DrawScope.drawMarkers(
    first: Offset,
    last: Offset,
    style: RouteCanvasStyle,
    strokePx: Float,
    colors: AppColors,
) {
    val radius = strokePx * MARKER_RADIUS_RATIO
    drawCircle(colors.routeStart, radius, first)
    if (style == RouteCanvasStyle.LIVE) {
        drawCircle(colors.routeStart, radius, last)
    } else {
        drawCircle(colors.routeLine, radius, last, style = Stroke(width = strokePx * END_RING_STROKE_RATIO))
    }
}

@ComponentPreviews
@Composable
private fun RouteCanvasPreview() {
    val previewRoute =
        ProjectedRoute(
            points =
                listOf(
                    NormalizedPoint(0.1f, 0.8f, 0),
                    NormalizedPoint(0.3f, 0.3f, 0),
                    NormalizedPoint(0.5f, 0.6f, 0),
                    NormalizedPoint(0.65f, 0.5f, 1),
                    NormalizedPoint(0.9f, 0.2f, 1),
                ),
            isDot = false,
        )
    RunPreviewSurface {
        RouteCanvas(
            route = previewRoute,
            style = RouteCanvasStyle.CARD,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp),
        )
        RouteCanvas(
            route = previewRoute,
            style = RouteCanvasStyle.LIVE,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
        )
        RouteCanvas(route = previewRoute, style = RouteCanvasStyle.THUMBNAIL, modifier = Modifier.size(56.dp))
    }
}
