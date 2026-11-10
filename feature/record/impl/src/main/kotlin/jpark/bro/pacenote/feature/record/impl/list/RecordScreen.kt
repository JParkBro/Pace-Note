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
import jpark.bro.pacenote.core.designsystem.component.RunSegment
import jpark.bro.pacenote.core.designsystem.component.RunSegmentedControl
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.domain.stats.PeriodSummary
import jpark.bro.pacenote.core.domain.stats.StatsPeriod
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RunTotals
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.record.impl.R
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 기록 탭(R1)의 Root: ViewModel 연결과 Event 처리 */
@Composable
internal fun RecordRoot(
    onStartRun: () -> Unit,
    viewModel: RecordViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            RecordEvent.StartRun -> onStartRun()
        }
    }
    RecordScreen(state, state.items.collectAsLazyPagingItems(), viewModel::onAction)
}

@Composable
private fun RecordScreen(
    state: RecordState,
    items: LazyPagingItems<RecordItem>,
    onAction: (RecordAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RunTopBar(title = stringResource(R.string.record_title)) },
    ) { padding ->
        if (state.isEmpty) {
            RecordEmpty(onAction, Modifier.padding(padding))
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            RunSegmentedControl(
                segments =
                    listOf(
                        RunSegment(stringResource(R.string.record_tab_list)),
                        RunSegment(stringResource(R.string.record_tab_stats)),
                    ),
                selectedIndex = state.tab.ordinal,
                onSelect = { onAction(RecordAction.TabSelected(RecordTab.entries[it])) },
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile).testTag("record_tabs"),
            )
            when (state.tab) {
                RecordTab.LIST -> RecordList(state, items, PaddingValues(bottom = AppSpacing.lg))
                RecordTab.STATS -> RecordStatsContent(state.stats, state.distanceUnit, state.thisYear, onAction)
            }
        }
    }
}

@Composable
private fun RecordList(
    state: RecordState,
    items: LazyPagingItems<RecordItem>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("record_list"),
        contentPadding = contentPadding,
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.key },
            contentType = items.itemContentType { it::class },
        ) { index ->
            when (val item = items[index]) {
                is RecordItem.MonthHeader -> {
                    MonthHeader(item.month, state.monthTotals[item.month], state.distanceUnit, state.thisYear)
                }

                is RecordItem.RunRow -> {
                    RunRowItem(item, state.distanceUnit)
                }

                null -> {
                    Unit
                }
            }
        }
    }
}

// 미리보기: 샘플 시나리오(product-spec 9장) 10월 3회 · 9월 1회
@Suppress("MagicNumber")
private val PREVIEW_ROWS: List<RecordItem> =
    listOf(
        RecordItem.MonthHeader(YearMonth.of(2026, 10)),
        RecordItem.RunRow(
            "1028",
            YearMonth.of(2026, 10),
            1_793_137_200_000L,
            "Asia/Seoul",
            5_210.0,
            1_812_000L,
            347.8,
            emptyList(),
        ),
        RecordItem.RunRow(
            "1027",
            YearMonth.of(2026, 10),
            1_793_099_700_000L,
            "Asia/Seoul",
            3_210.0,
            1_103_000L,
            343.6,
            emptyList(),
        ),
        RecordItem.RunRow(
            "1026",
            YearMonth.of(2026, 10),
            1_792_966_500_000L,
            "Asia/Seoul",
            3_980.0,
            1_445_000L,
            363.1,
            emptyList(),
        ),
        RecordItem.MonthHeader(YearMonth.of(2026, 9)),
        RecordItem.RunRow(
            "0929",
            YearMonth.of(2026, 9),
            1_790_632_800_000L,
            "Asia/Seoul",
            10_050.0,
            3_734_000L,
            371.5,
            emptyList(),
        ),
    )

private const val PREVIEW_YEAR = 2026

// 이번 주(10.26 – 11.1) 12.40 km 3회, 지난주 10.30 km
@Suppress("MagicNumber")
private val PREVIEW_STATS =
    RecordStats(
        current =
            PeriodSummary(
                period = StatsPeriod.WEEK,
                start = LocalDate.of(2026, 10, 26),
                distanceM = 12_400.0,
                movingMs = 4_360_000L,
                runCount = 3,
                bars = listOf(3_980.0, 3_210.0, 5_210.0, 0.0, 0.0, 0.0, 0.0),
                barStarts = List(7) { LocalDate.of(2026, 10, 26).plusDays(it.toLong()) },
            ),
        previousDistanceM = 10_300.0,
        averagePaceSecPerKm = 351.6,
        today = LocalDate.of(2026, 10, 29),
        canGoNext = false,
    )

@Suppress("MagicNumber")
private val PREVIEW_TOTALS =
    mapOf(YearMonth.of(2026, 10) to RunTotals(12_400.0, 3), YearMonth.of(2026, 9) to RunTotals(10_050.0, 1))

private fun previewState() =
    RecordState(
        loading = false,
        items = flowOf(PagingData.from(PREVIEW_ROWS)),
        monthTotals = PREVIEW_TOTALS,
        thisYear = PREVIEW_YEAR,
    )

@Composable
private fun Preview(
    state: RecordState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) {
        RecordScreen(state, state.items.collectAsLazyPagingItems(), {})
    }
}

@ScreenPreviews
@Composable
private fun RecordPreview() = Preview(previewState())

@ScreenPreviews
@Composable
private fun RecordInkDarkPreview() = Preview(previewState(), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun RecordStatsPreview() = Preview(previewState().copy(tab = RecordTab.STATS, stats = PREVIEW_STATS))

@ScreenPreviews
@Composable
private fun RecordEmptyPreview() = Preview(RecordState(loading = false))
