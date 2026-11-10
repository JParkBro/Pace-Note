package jpark.bro.pacenote.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunStat
import jpark.bro.pacenote.core.designsystem.component.RunStatGrid
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.domain.tracking.SplitRow
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RoutePoint
import kotlin.math.abs
import kotlin.math.roundToInt

// 끝난 기록 한 건의 요약 조각. 결과(P6, run)와 상세(R3, record)가 같은 모양으로 쓴다.

private const val ROUTE_ASPECT = 1.6f
private const val ROUTE_PADDING = 0.1f
private const val METERS_PER_KM = 1_000.0
private const val SPLIT_EPSILON_KM = 0.001
private const val NO_VALUE = "–"

/** 경로 카드 (흰 선, 지도 없음) */
@Composable
fun RunRouteCard(
    route: List<RoutePoint>,
    modifier: Modifier = Modifier,
) {
    val projected = remember(route) { RouteProjector.project(route, ROUTE_ASPECT, ROUTE_PADDING) }
    Box(modifier = modifier.fillMaxWidth().aspectRatio(ROUTE_ASPECT)) {
        RouteCanvas(
            route = projected,
            style = RouteCanvasStyle.CARD,
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(R.string.ui_run_route),
        )
    }
}

/** 2×2 수치: 거리 · 시간 · 평균 페이스 · 칼로리 */
@Composable
fun RunSummaryStats(
    distanceM: Double,
    movingMs: Long,
    averagePaceSecPerKm: Double?,
    calories: Int?,
    unit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    RunStatGrid(
        stats =
            listOf(
                RunStat(
                    value = RunFormat.distance(distanceM, unit),
                    label = stringResource(R.string.ui_run_distance, stringResource(RunFormat.unitLabelRes(unit))),
                ),
                RunStat(RunFormat.duration(movingMs), stringResource(R.string.ui_run_time)),
                RunStat(
                    value = RunFormat.pace(averagePaceSecPerKm, unit),
                    label = stringResource(R.string.ui_run_average_pace),
                    hasValue = averagePaceSecPerKm != null,
                ),
                RunStat(
                    value = calories?.toString() ?: NO_VALUE,
                    label = stringResource(R.string.ui_run_calories),
                    hasValue = calories != null,
                ),
            ),
        modifier = modifier.testTag("run_summary_stats"),
    )
}

/** 구간 표. 구간은 항상 km라 페이스도 km당으로 보여준다. */
@Composable
fun RunSplitTable(
    splits: List<SplitRow>,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val km = stringResource(RunFormat.unitLabelRes(DistanceUnit.KM))
    RunSection(title = stringResource(R.string.ui_run_splits), modifier = modifier.testTag("run_split_table")) {
        splits.forEach { split ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text(text = split.index.toString(), style = typography.bodyMd, color = colors.textTertiary)
                Text(
                    text = "${splitDistance(split.endDistanceM)} $km",
                    style = typography.bodyMd,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = RunFormat.pace(split.paceSecPerKm, DistanceUnit.KM),
                    style = typography.labelLg,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

/** 1km 배수면 "3", 마지막 짧은 구간이면 "5.21" */
private fun splitDistance(endDistanceM: Double): String {
    val km = endDistanceM / METERS_PER_KM
    val whole = km.roundToInt()
    return if (abs(km - whole) <
        SPLIT_EPSILON_KM
    ) {
        whole.toString()
    } else {
        RunFormat.distance(endDistanceM, DistanceUnit.KM)
    }
}
