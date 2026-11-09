package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

private const val PACE_RANGE_PADDING = 0.1f

data class RunBar(
    val label: String,
    val value: Float,
    val selected: Boolean = false,
)

/** 요일 · 주차 막대. 막대는 accent, 선택 막대는 primary다. 값이 0이면 바닥선만 보인다. */
@Composable
fun RunBarChart(
    bars: List<RunBar>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val max = bars.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f
    Column(modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription }) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(bar.value.coerceAtLeast(0f) / max)
                                .clip(AppShapes.sm)
                                .background(if (bar.selected) colors.primary else colors.accent),
                    )
                }
            }
        }
        ChartBaseline()
        ChartLabels(bars.map { it.label })
    }
}

/** km별 페이스 꺾은선. 빠를수록 위로 그린다. 복원한 기록도 그릴 수 있게 구간 값만 받는다. */
@Composable
fun RunPaceChart(
    paceSecPerKm: List<Float>,
    labels: List<String>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Column(modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription }) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp),
        ) {
            val offsets = paceOffsets(paceSecPerKm, size.width, size.height, AppSpacing.sm.toPx())
            val stroke = 2.dp.toPx()
            if (offsets.size == 1) {
                drawCircle(colors.accent, stroke, offsets.single())
            } else if (offsets.isNotEmpty()) {
                val path =
                    Path().apply {
                        moveTo(offsets.first().x, offsets.first().y)
                        offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                drawPath(path, colors.accent, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
        ChartBaseline()
        ChartLabels(labels)
    }
}

/** 점은 아래 라벨 칸의 가운데에 놓는다. */
private fun paceOffsets(
    paces: List<Float>,
    width: Float,
    height: Float,
    gap: Float,
): List<Offset> {
    val fastest = paces.minOrNull() ?: return emptyList()
    val range = ((paces.maxOrNull() ?: fastest) - fastest).takeIf { it > 0f } ?: 1f
    val usable = height * (1 - 2 * PACE_RANGE_PADDING)
    val cell = (width - gap * (paces.size - 1)) / paces.size
    return paces.mapIndexed { index, pace ->
        Offset(index * (cell + gap) + cell / 2, height * PACE_RANGE_PADDING + usable * (pace - fastest) / range)
    }
}

@Composable
private fun ChartBaseline() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppThemeTokens.colors.border),
    )
}

@Composable
private fun ChartLabels(labels: List<String>) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ChartLabelHeight),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = AppThemeTokens.typography.labelSm,
                color = AppThemeTokens.colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private val ChartLabelHeight = 24.dp

@ComponentPreviews
@Composable
private fun RunChartsPreview() {
    RunPreviewSurface {
        RunBarChart(
            bars =
                listOf(
                    RunBar("월", 3.98f),
                    RunBar("화", 3.21f),
                    RunBar("수", 5.21f, selected = true),
                    RunBar("목", 0f),
                    RunBar("금", 0f),
                    RunBar("토", 0f),
                    RunBar("일", 0f),
                ),
            contentDescription = "요일별 거리",
        )
        RunPaceChart(
            paceSecPerKm = listOf(352f, 344f, 340f, 350f, 347f),
            labels = listOf("1", "2", "3", "4", "5"),
            contentDescription = "구간별 페이스",
        )
    }
}
