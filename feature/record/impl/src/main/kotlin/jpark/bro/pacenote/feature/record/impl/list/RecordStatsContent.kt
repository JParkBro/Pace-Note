package jpark.bro.pacenote.feature.record.impl.list

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunBar
import jpark.bro.pacenote.core.designsystem.component.RunBarChart
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunFilterChip
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunInfoBox
import jpark.bro.pacenote.core.designsystem.component.RunStatSize
import jpark.bro.pacenote.core.designsystem.component.RunStatTile
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import jpark.bro.pacenote.core.domain.stats.PeriodStats
import jpark.bro.pacenote.core.domain.stats.StatsPeriod
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.record.impl.R
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import jpark.bro.pacenote.core.designsystem.R as DesignR

private val CHART_HEIGHT = 160.dp
private val DAY_FORMAT = DateTimeFormatter.ofPattern("M.d")
private const val SAME_DISTANCE_M = 10.0

/** 통계(R2): 주 / 월 칩 · 기간 이동 · 총 거리 · 막대 · 요약 · 지난 기간 대비 */
@Composable
internal fun RecordStatsContent(
    stats: RecordStats?,
    unit: DistanceUnit,
    thisYear: Int,
    onAction: (RecordAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md)
                .testTag("record_stats"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
    ) {
        if (stats == null) return@Column
        PeriodBar(stats, thisYear, onAction)
        RunCard {
            Column(
                modifier = Modifier.padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                RunStatTile(
                    value = RunFormat.distance(stats.current.distanceM, unit),
                    label =
                        "${stringResource(R.string.record_stats_total_distance)} " +
                            "(${stringResource(RunFormat.unitLabelRes(unit))})",
                    size = RunStatSize.LARGE,
                )
                RunBarChart(
                    bars = bars(stats),
                    contentDescription = stringResource(R.string.record_stats_chart),
                    modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT),
                )
                Summary(stats, unit)
                compareText(
                    stats,
                    unit,
                )?.let { RunInfoBox(text = it, modifier = Modifier.testTag("record_stats_compare")) }
            }
        }
    }
}

@Composable
private fun PeriodBar(
    stats: RecordStats,
    thisYear: Int,
    onAction: (RecordAction) -> Unit,
) {
    val period = stats.current.period
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.weight(1f)) {
            RunFilterChip(
                text = stringResource(R.string.record_stats_week),
                selected = period == StatsPeriod.WEEK,
                onClick = { onAction(RecordAction.PeriodSelected(StatsPeriod.WEEK)) },
            )
            RunFilterChip(
                text = stringResource(R.string.record_stats_month),
                selected = period == StatsPeriod.MONTH,
                onClick = { onAction(RecordAction.PeriodSelected(StatsPeriod.MONTH)) },
            )
        }
        RunIconButton(
            icon = DesignR.drawable.ic_chevron_left,
            contentDescription = stringResource(R.string.record_stats_previous),
            onClick = { onAction(RecordAction.PreviousPeriodClicked) },
        )
        Text(
            text = periodLabel(stats, thisYear),
            style = AppThemeTokens.typography.labelLg,
            color = AppThemeTokens.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppSpacing.sm).testTag("record_stats_period"),
        )
        RunIconButton(
            icon = DesignR.drawable.ic_chevron_right,
            contentDescription = stringResource(R.string.record_stats_next),
            onClick = { onAction(RecordAction.NextPeriodClicked) },
            enabled = stats.canGoNext,
        )
    }
}

@Composable
private fun periodLabel(
    stats: RecordStats,
    thisYear: Int,
): String {
    val start = stats.current.start
    return when (stats.current.period) {
        StatsPeriod.WEEK -> {
            stringResource(
                R.string.record_stats_week_range,
                DAY_FORMAT.format(start),
                DAY_FORMAT.format(PeriodStats.endOf(StatsPeriod.WEEK, start).minusDays(1)),
            )
        }

        StatsPeriod.MONTH -> {
            if (start.year == thisYear) {
                stringResource(R.string.record_month, start.monthValue)
            } else {
                stringResource(R.string.record_month_with_year, start.year, start.monthValue)
            }
        }
    }
}

@Composable
private fun bars(stats: RecordStats): List<RunBar> {
    val current = stats.current
    val todayBar =
        when (current.period) {
            StatsPeriod.WEEK -> stats.today
            StatsPeriod.MONTH -> WeeklyProgress.weekStart(stats.today)
        }
    return current.bars.mapIndexed { index, distanceM ->
        val barStart = current.barStarts[index]
        val label =
            when (current.period) {
                StatsPeriod.WEEK -> barStart.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                StatsPeriod.MONTH -> stringResource(R.string.record_stats_week_bar, index + 1)
            }
        RunBar(label = label, value = distanceM.toFloat(), selected = barStart == todayBar)
    }
}

@Composable
private fun Summary(
    stats: RecordStats,
    unit: DistanceUnit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        RunStatTile(
            value = RunFormat.distance(stats.current.distanceM, unit),
            label = "${stringResource(
                R.string.record_stats_distance,
            )} (${stringResource(RunFormat.unitLabelRes(unit))})",
        )
        RunStatTile(
            value = stringResource(R.string.record_stats_count_value, stats.current.runCount),
            label = stringResource(R.string.record_stats_count),
        )
        RunStatTile(
            value = RunFormat.pace(stats.averagePaceSecPerKm, unit),
            label = stringResource(R.string.record_stats_average_pace),
            hasValue = stats.averagePaceSecPerKm != null,
        )
    }
}

/** "지난주보다 2.10km 더 달렸어요". 두 기간 모두 기록이 없으면 null */
@Composable
private fun compareText(
    stats: RecordStats,
    unit: DistanceUnit,
): String? {
    val current = stats.current.distanceM
    val previous = stats.previousDistanceM
    if (current == 0.0 && previous == 0.0) return null
    val diff = current - previous
    val week = stats.current.period == StatsPeriod.WEEK

    @StringRes val res: Int =
        when {
            abs(
                diff,
            ) < SAME_DISTANCE_M -> if (week) R.string.record_stats_same_week else R.string.record_stats_same_month

            diff > 0 -> if (week) R.string.record_stats_more_week else R.string.record_stats_more_month

            else -> if (week) R.string.record_stats_less_week else R.string.record_stats_less_month
        }
    return stringResource(res, RunFormat.distance(abs(diff), unit), stringResource(RunFormat.unitLabelRes(unit)))
}
