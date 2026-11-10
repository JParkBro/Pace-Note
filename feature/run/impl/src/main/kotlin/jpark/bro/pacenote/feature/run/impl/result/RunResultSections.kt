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

private const val PERCENT = 100

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
