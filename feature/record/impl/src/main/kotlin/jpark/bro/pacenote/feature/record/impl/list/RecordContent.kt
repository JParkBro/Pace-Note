package jpark.bro.pacenote.feature.record.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunListItem
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.record.impl.R
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import jpark.bro.pacenote.core.designsystem.R as DesignR

private val THUMBNAIL_SIZE = 56.dp
private const val THUMBNAIL_PADDING = 0.15f
private val RUN_DATE_FORMAT = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)

@Composable
internal fun MonthHeader(
    month: YearMonth,
    totals: RunTotals?,
    unit: DistanceUnit,
    thisYear: Int,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.marginMobile)
                .padding(top = AppSpacing.lg, bottom = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                if (month.year == thisYear) {
                    stringResource(R.string.record_month, month.monthValue)
                } else {
                    stringResource(R.string.record_month_with_year, month.year, month.monthValue)
                },
            style = AppThemeTokens.typography.headlineSm,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (totals != null) {
            Text(
                text =
                    stringResource(
                        R.string.record_month_totals,
                        RunFormat.distance(totals.distanceM, unit),
                        stringResource(RunFormat.unitLabelRes(unit)),
                        totals.runCount,
                    ),
                style = AppThemeTokens.typography.bodySm,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
internal fun RunRowItem(
    run: RecordItem.RunRow,
    unit: DistanceUnit,
    onClick: () -> Unit,
) {
    RunCard(modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.xs)) {
        RunListItem(
            title = "${RunFormat.distance(run.distanceM, unit)} ${stringResource(RunFormat.unitLabelRes(unit))}",
            subtitle =
                listOf(
                    RUN_DATE_FORMAT.format(Instant.ofEpochMilli(run.startedAt).atZone(ZoneId.of(run.zoneId))),
                    stringResource(
                        R.string.record_run_summary,
                        RunFormat.duration(run.movingMs),
                        RunFormat.pace(run.averagePaceSecPerKm, unit),
                    ),
                ).joinToString(" · "),
            leading = { RunThumbnail(run) },
            onClick = onClick,
            modifier = Modifier.testTag("record_run_${run.id}"),
        )
    }
}

@Composable
private fun RunThumbnail(run: RecordItem.RunRow) {
    val projected = remember(run.route) { RouteProjector.project(run.route, 1f, THUMBNAIL_PADDING) }
    RouteCanvas(
        route = projected,
        style = RouteCanvasStyle.THUMBNAIL,
        modifier = Modifier.size(THUMBNAIL_SIZE),
        contentDescription = stringResource(R.string.record_run_route),
    )
}

@Composable
internal fun RecordEmpty(
    onAction: (RecordAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.marginMobile).testTag("record_empty"),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.record_empty),
            style = AppThemeTokens.typography.bodyLg,
            color = AppThemeTokens.colors.textSecondary,
        )
        RunPrimaryButton(
            text = stringResource(R.string.record_start_run),
            onClick = { onAction(RecordAction.StartRunClicked) },
            icon = DesignR.drawable.ic_play_arrow,
            modifier = Modifier.fillMaxWidth().testTag("record_start_run"),
        )
    }
}
