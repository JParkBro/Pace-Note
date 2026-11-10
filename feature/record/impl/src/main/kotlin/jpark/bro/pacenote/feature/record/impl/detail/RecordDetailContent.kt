package jpark.bro.pacenote.feature.record.impl.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunPaceChart
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle
import jpark.bro.pacenote.core.domain.stats.TimeOfDay
import jpark.bro.pacenote.core.domain.tracking.SplitRow
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.core.ui.RunRouteCard
import jpark.bro.pacenote.core.ui.RunSplitTable
import jpark.bro.pacenote.core.ui.RunSummaryStats
import jpark.bro.pacenote.feature.record.impl.R
import jpark.bro.pacenote.feature.record.impl.ui.defaultTitleText
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import jpark.bro.pacenote.core.designsystem.R as DesignR

private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("M월 d일 (E) a h:mm", Locale.KOREAN)
private val PACE_CHART_HEIGHT = 140.dp

/** 경과 시간(일시정지 포함) · 시작 · 종료 시각 */
@Composable
internal fun TimesCard(state: RecordDetailState) {
    val zone = ZoneId.of(state.zoneId)
    RunCard(modifier = Modifier.testTag("record_detail_times")) {
        Column(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
            TimeRow(stringResource(R.string.record_detail_elapsed), RunFormat.duration(state.elapsedMs))
            TimeRow(
                stringResource(R.string.record_detail_started),
                DATE_TIME_FORMAT.format(Instant.ofEpochMilli(state.startedAt).atZone(zone)),
            )
            state.endedAt?.let {
                TimeRow(
                    stringResource(R.string.record_detail_ended),
                    DATE_TIME_FORMAT.format(Instant.ofEpochMilli(it).atZone(zone)),
                )
            }
        }
    }
}

@Composable
private fun TimeRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs)) {
        Text(
            text = label,
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = AppThemeTokens.typography.bodyMd, color = AppThemeTokens.colors.textPrimary)
    }
}

/** km별 페이스 꺾은선 (페이스를 구할 수 있는 구간이 2개 이상일 때) */
@Composable
internal fun PaceChart(splits: List<SplitRow>) {
    val points = splits.mapNotNull { split -> split.paceSecPerKm?.let { split.index to it.toFloat() } }
    if (points.size < 2) return
    RunSection(title = stringResource(R.string.record_detail_pace_title)) {
        RunPaceChart(
            paceSecPerKm = points.map { it.second },
            labels = points.map { it.first.toString() },
            contentDescription = stringResource(R.string.record_detail_pace_chart),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        AppSpacing.md,
                    ).height(PACE_CHART_HEIGHT)
                    .testTag("record_detail_pace_chart"),
        )
    }
}

@Composable
internal fun Missing(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.record_detail_missing),
            style = AppThemeTokens.typography.bodyLg,
            color = AppThemeTokens.colors.textSecondary,
        )
    }
}
