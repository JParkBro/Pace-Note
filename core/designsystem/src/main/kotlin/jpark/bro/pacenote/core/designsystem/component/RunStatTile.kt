package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

enum class RunStatSize {
    /** 러닝 중 거리 */
    HUGE,

    /** 러닝 중 시간 · 페이스 */
    LARGE,

    /** 결과 · 상세 수치 */
    MEDIUM,
}

/**
 * 수치 타일. 값은 항상 text-primary, 라벨은 text-secondary다 (theme-guide.md 5.5).
 * 페이스처럼 아직 값이 없으면 [hasValue] = false로 넘겨 `–′––″`를 text-tertiary로 표시한다.
 */
@Composable
fun RunStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    size: RunStatSize = RunStatSize.MEDIUM,
    hasValue: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val colors = AppThemeTokens.colors
    Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
        Text(
            text = value,
            style = size.valueStyle(),
            color = if (hasValue) colors.textPrimary else colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(text = label, style = AppThemeTokens.typography.bodySm, color = colors.textSecondary)
    }
}

data class RunStat(
    val value: String,
    val label: String,
    val hasValue: Boolean = true,
)

/** 수치 타일 격자 (결과 · 상세의 2×2) */
@Composable
fun RunStatGrid(
    stats: List<RunStat>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        stats.chunked(columns).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { stat ->
                    RunStatTile(stat.value, stat.label, Modifier.weight(1f), hasValue = stat.hasValue)
                }
                repeat(columns - row.size) { Column(modifier = Modifier.weight(1f)) {} }
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun RunStatSize.valueStyle(): TextStyle =
    when (this) {
        RunStatSize.HUGE -> AppThemeTokens.typography.metricHuge
        RunStatSize.LARGE -> AppThemeTokens.typography.metricLg
        RunStatSize.MEDIUM -> AppThemeTokens.typography.metricMd
    }

@ComponentPreviews
@Composable
private fun RunStatTilePreview() {
    RunPreviewSurface {
        RunStatTile("3.42", "거리 (km)", size = RunStatSize.HUGE)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
            RunStatTile("20:31", "시간", size = RunStatSize.LARGE)
            RunStatTile("–′––″", "현재 페이스", size = RunStatSize.LARGE, hasValue = false)
        }
        RunStatGrid(
            stats =
                listOf(
                    RunStat("5.21 km", "거리"),
                    RunStat("30:12", "시간"),
                    RunStat("5′48″", "평균 페이스"),
                    RunStat("351 kcal", "칼로리"),
                ),
        )
    }
}
