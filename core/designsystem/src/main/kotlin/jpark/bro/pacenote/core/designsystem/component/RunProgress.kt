package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

private const val RING_START_DEGREES = -90f
private const val FULL_CIRCLE_DEGREES = 360f

/** 진행바. [progress]가 1을 넘으면 가득 찬 상태로 그린다 (theme-guide.md 5.6). */
@Composable
fun RunProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val filled = progress.coerceIn(0f, 1f)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.fill)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(filled, 0f..1f) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(filled)
                    .clip(AppShapes.full)
                    .background(AppThemeTokens.progressBrush),
        )
    }
}

/** 진행 링. 가운데에 [content]를 놓는다. [progress]가 1을 넘으면 가득 찬 상태로 그린다. */
@Composable
fun RunProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val track = AppThemeTokens.colors.fill
    val brush = AppThemeTokens.progressBrush
    val filled = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(filled, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, FULL_CIRCLE_DEGREES, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(
                brush = brush,
                startAngle = RING_START_DEGREES,
                sweepAngle = FULL_CIRCLE_DEGREES * filled,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@ComponentPreviews
@Composable
private fun RunProgressPreview() {
    RunPreviewSurface {
        RunProgressBar(progress = 0.83f)
        RunProgressBar(progress = 1.2f)
        RunProgressRing(progress = 0.83f, modifier = Modifier.size(200.dp)) {
            Text(
                text = "12.4",
                style = AppThemeTokens.typography.metricXl,
                color = AppThemeTokens.colors.textPrimary,
            )
        }
    }
}
