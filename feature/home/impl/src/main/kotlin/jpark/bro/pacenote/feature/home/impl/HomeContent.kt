package jpark.bro.pacenote.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.designsystem.component.RunBadge
import jpark.bro.pacenote.core.designsystem.component.RunBadgeTone
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunListItem
import jpark.bro.pacenote.core.designsystem.component.RunProgressRing
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunStatTile
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.ui.RunFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val RING_SIZE = 220.dp
private val THUMBNAIL_SIZE = 56.dp
private const val THUMBNAIL_PADDING = 0.15f
private const val PERCENT = 100
private val RECENT_RUN_FORMAT = DateTimeFormatter.ofPattern("M.d (E) a h:mm", Locale.KOREAN)

/** 이번 주 진행 카드: 목표 링(목표가 없으면 이번 주 거리만) + 횟수 · 시간 · 평균 페이스 */
@Composable
internal fun WeekCard(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    RunCard(modifier = Modifier.testTag("home_week")) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            val goal = state.goal
            if (goal == null) {
                WeekDistance(state.distanceM, state.distanceUnit)
                TextButton(
                    onClick = { onAction(HomeAction.SetGoalClicked) },
                    modifier = Modifier.testTag("home_set_goal"),
                ) {
                    Text(
                        text = stringResource(R.string.home_set_goal),
                        style = AppThemeTokens.typography.labelLg,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                }
            } else {
                GoalRing(state, goal)
                if (goal.achieved) {
                    RunBadge(
                        text = stringResource(R.string.home_goal_achieved),
                        tone = RunBadgeTone.SUCCESS,
                        modifier = Modifier.testTag("home_goal_achieved"),
                    )
                }
            }
            WeekSummary(state)
        }
    }
}

@Composable
private fun WeekDistance(
    distanceM: Double,
    unit: DistanceUnit,
) {
    RunStatTile(
        value = RunFormat.distance(distanceM, unit),
        label = "${stringResource(R.string.home_week_distance)} (${stringResource(RunFormat.unitLabelRes(unit))})",
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("home_week_distance"),
    )
}

@Composable
private fun GoalRing(
    state: HomeState,
    goal: HomeGoal,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    val progressLabel = stringResource(R.string.home_goal_progress)
    RunProgressRing(
        progress = goal.ratio.toFloat(),
        modifier =
            Modifier
                .size(RING_SIZE)
                .testTag("home_goal_ring")
                .semantics { contentDescription = "$progressLabel ${(goal.ratio * PERCENT).roundToInt()}%" },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val (done, target) =
                when (goal.type) {
                    GoalType.DISTANCE -> {
                        val unit = stringResource(RunFormat.unitLabelRes(state.distanceUnit))
                        RunFormat.distance(state.distanceM, state.distanceUnit) to
                            stringResource(
                                R.string.home_goal_distance,
                                (goal.value / RunFormat.unitMeters(state.distanceUnit)).roundToInt().toString(),
                                unit,
                            )
                    }

                    GoalType.COUNT -> {
                        state.runCount.toString() to stringResource(R.string.home_goal_count, goal.value.roundToInt())
                    }
                }
            Text(text = done, style = typography.metricXl, color = colors.textPrimary)
            Text(text = target, style = typography.bodyMd, color = colors.textSecondary)
        }
    }
}

@Composable
private fun WeekSummary(state: HomeState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        RunStatTile(
            value = stringResource(R.string.home_run_count_value, state.runCount),
            label = stringResource(R.string.home_run_count),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        RunStatTile(
            value = RunFormat.duration(state.movingMs),
            label = stringResource(R.string.home_time),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        RunStatTile(
            value = RunFormat.pace(state.averagePaceSecPerKm, state.distanceUnit),
            label = stringResource(R.string.home_average_pace),
            hasValue = state.averagePaceSecPerKm != null,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
    }
}

/** 최근 기록 2개. 없으면 빈 상태 */
@Composable
internal fun RecentRuns(
    runs: List<HomeRun>,
    unit: DistanceUnit,
    onAction: (HomeAction) -> Unit,
) {
    if (runs.isEmpty()) {
        Text(
            text = stringResource(R.string.home_empty),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg).testTag("home_empty"),
        )
        return
    }
    val unitLabel = stringResource(RunFormat.unitLabelRes(unit))
    RunSection(title = stringResource(R.string.home_recent_runs), modifier = Modifier.testTag("home_recent_runs")) {
        runs.forEach { run ->
            RunListItem(
                title = "${RunFormat.distance(run.distanceM, unit)} $unitLabel",
                subtitle =
                    stringResource(
                        R.string.home_recent_run_summary,
                        RECENT_RUN_FORMAT.format(Instant.ofEpochMilli(run.startedAt).atZone(ZoneId.of(run.zoneId))),
                        RunFormat.duration(run.movingMs),
                        RunFormat.pace(run.averagePaceSecPerKm, unit),
                    ),
                leading = { RunThumbnail(run) },
                onClick = { onAction(HomeAction.RunClicked(run.id)) },
                modifier = Modifier.testTag("home_run_${run.id}"),
            )
        }
    }
}

@Composable
private fun RunThumbnail(run: HomeRun) {
    val projected = remember(run.route) { RouteProjector.project(run.route, 1f, THUMBNAIL_PADDING) }
    RouteCanvas(
        route = projected,
        style = RouteCanvasStyle.THUMBNAIL,
        modifier = Modifier.size(THUMBNAIL_SIZE),
        contentDescription = stringResource(R.string.home_recent_run_route),
    )
}
