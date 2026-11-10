package jpark.bro.pacenote.widget

import androidx.compose.runtime.Composable
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.GoalType

// 크기별 Preview (widget/CLAUDE.md): 목표 0% · 83% · 100% 초과, 목표 없음. 다크는 기기 설정으로 바뀐다

private const val WIDE_WIDTH = 250
private const val WIDE_HEIGHT = 110
private const val SMALL_SIZE = 110

private fun sample(
    distanceM: Double,
    ratio: Double,
    goal: Boolean = true,
    theme: AppThemeType = AppThemeType.VOLT,
) = WeeklyWidgetData(
    distanceM = distanceM,
    runCount = 3,
    goalType = if (goal) GoalType.DISTANCE else null,
    goalValue = if (goal) GOAL_M else null,
    ratio = ratio,
    distanceUnit = DistanceUnit.KM,
    themeType = theme,
)

private const val GOAL_M = 15_000.0

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = WIDE_WIDTH, heightDp = WIDE_HEIGHT)
@Composable
private fun WeeklyGoalPreview() = WeeklyGoalContent(sample(12_400.0, 0.8267))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = WIDE_WIDTH, heightDp = WIDE_HEIGHT)
@Composable
private fun WeeklyGoalZeroPreview() = WeeklyGoalContent(sample(0.0, 0.0, theme = AppThemeType.SUNRISE))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = WIDE_WIDTH, heightDp = WIDE_HEIGHT)
@Composable
private fun WeeklyGoalOverPreview() = WeeklyGoalContent(sample(17_200.0, 1.1467, theme = AppThemeType.INK))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = WIDE_WIDTH, heightDp = WIDE_HEIGHT)
@Composable
private fun WeeklyGoalNoGoalPreview() = WeeklyGoalContent(sample(12_400.0, 0.0, goal = false))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = SMALL_SIZE, heightDp = SMALL_SIZE)
@Composable
private fun WeeklyGoalSmallPreview() = WeeklyGoalSmallContent(sample(12_400.0, 0.8267))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = SMALL_SIZE, heightDp = SMALL_SIZE)
@Composable
private fun WeeklyGoalSmallOverPreview() = WeeklyGoalSmallContent(sample(17_200.0, 1.1467, theme = AppThemeType.RIVER))

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = SMALL_SIZE, heightDp = SMALL_SIZE)
@Composable
private fun WeeklyGoalSmallNoGoalPreview() = WeeklyGoalSmallContent(sample(5_210.0, 0.0, goal = false))
