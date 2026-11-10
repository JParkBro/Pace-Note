package jpark.bro.pacenote.feature.run.impl.result

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunProgressBar
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunStat
import jpark.bro.pacenote.core.designsystem.component.RunStatGrid
import jpark.bro.pacenote.core.designsystem.component.RunTextField
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.run.impl.R
import kotlin.math.abs
import kotlin.math.roundToInt

private const val ROUTE_ASPECT = 1.6f
private const val ROUTE_PADDING = 0.1f
private const val PERCENT = 100
private const val SPLIT_EPSILON_KM = 0.001
private const val NO_VALUE = "–"
private const val METERS_PER_KM = 1_000.0

@Composable
internal fun ResultRoute(route: List<RoutePoint>) {
    val projected = remember(route) { RouteProjector.project(route, ROUTE_ASPECT, ROUTE_PADDING) }
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(ROUTE_ASPECT)) {
        RouteCanvas(
            route = projected,
            style = RouteCanvasStyle.CARD,
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(R.string.run_result_route),
        )
    }
}

@Composable
internal fun ResultStats(state: RunResultState) {
    val unit = state.distanceUnit
    RunStatGrid(
        stats =
            listOf(
                RunStat(
                    value = RunFormat.distance(state.distanceM, unit),
                    label = stringResource(R.string.run_result_distance, stringResource(RunFormat.unitLabelRes(unit))),
                ),
                RunStat(RunFormat.duration(state.movingMs), stringResource(R.string.run_result_time)),
                RunStat(
                    value = RunFormat.pace(state.averagePaceSecPerKm, unit),
                    label = stringResource(R.string.run_result_average_pace),
                    hasValue = state.averagePaceSecPerKm != null,
                ),
                RunStat(
                    value = state.calories?.toString() ?: NO_VALUE,
                    label = stringResource(R.string.run_result_calories),
                    hasValue = state.calories != null,
                ),
            ),
        modifier = Modifier.testTag("run_result_stats"),
    )
}

@Composable
internal fun ResultGoalCard(
    goal: ResultGoal,
    unit: DistanceUnit,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    RunCard(modifier = Modifier.testTag("run_result_goal")) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.run_result_goal, (goal.ratio * PERCENT).roundToInt()),
                style = typography.bodyMd,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (goal.remaining > 0) {
                Text(text = remainingText(goal, unit), style = typography.bodySm, color = colors.textSecondary)
            }
        }
        RunProgressBar(progress = goal.ratio.toFloat(), modifier = Modifier.padding(top = AppSpacing.sm))
    }
}

@Composable
private fun remainingText(
    goal: ResultGoal,
    unit: DistanceUnit,
): String =
    when (goal.type) {
        GoalType.DISTANCE -> {
            stringResource(
                R.string.run_result_goal_remaining_distance,
                RunFormat.distance(goal.remaining, unit),
                stringResource(RunFormat.unitLabelRes(unit)),
            )
        }

        GoalType.COUNT -> {
            stringResource(R.string.run_result_goal_remaining_count, goal.remaining.roundToInt())
        }
    }

@Composable
internal fun ResultSplits(splits: List<ResultSplit>) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val km = stringResource(RunFormat.unitLabelRes(DistanceUnit.KM))
    RunSection(title = stringResource(R.string.run_result_splits), modifier = Modifier.testTag("run_result_splits")) {
        splits.forEach { split ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text(text = split.index.toString(), style = typography.bodyMd, color = colors.textTertiary)
                Text(
                    text = "${splitDistance(split.endDistanceM)} $km",
                    style = typography.bodyMd,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                // 구간은 항상 km라 페이스도 km당으로 보여준다
                Text(
                    text = RunFormat.pace(split.paceSecPerKm, DistanceUnit.KM),
                    style = typography.labelLg,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

@Composable
internal fun ResultMemo(
    memo: String,
    onMemoChange: (String) -> Unit,
) {
    RunTextField(
        value = memo,
        onValueChange = onMemoChange,
        placeholder = stringResource(R.string.run_result_memo_hint),
        singleLine = false,
        maxLength = RunResultState.MEMO_MAX_LENGTH,
        modifier = Modifier.fillMaxWidth().testTag("run_result_memo"),
    )
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
