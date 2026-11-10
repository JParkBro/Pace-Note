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

/** 기록 상세(R3)의 Root: ViewModel 연결과 Event 처리 */
@Composable
internal fun RecordDetailRoot(
    onBack: () -> Unit,
    onEdit: (runId: String) -> Unit,
    viewModel: RecordDetailViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is RecordDetailEvent.OpenEdit -> onEdit(event.runId)
            RecordDetailEvent.NavigateBack -> onBack()
        }
    }
    RecordDetailScreen(state, viewModel::onAction)
}

@Composable
private fun RecordDetailScreen(
    state: RecordDetailState,
    onAction: (RecordDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultTitle = state.defaultTitle
    val title = state.title ?: defaultTitle?.let { defaultTitleText(it) }.orEmpty()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = title,
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.record_back),
                        onClick = { onAction(RecordDetailAction.BackClicked) },
                    )
                },
                actions = {
                    if (!state.loading && !state.missing) {
                        TextButton(
                            onClick = { onAction(RecordDetailAction.EditClicked) },
                            modifier = Modifier.testTag("record_detail_edit"),
                        ) {
                            Text(
                                text = stringResource(R.string.record_detail_edit),
                                style = AppThemeTokens.typography.labelLg,
                                color = AppThemeTokens.colors.textSecondary,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> Unit
            state.missing -> Missing(Modifier.padding(padding))
            else -> DetailContent(state, Modifier.padding(padding))
        }
    }
}

@Composable
private fun DetailContent(
    state: RecordDetailState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
    ) {
        RunRouteCard(state.route)
        RunSummaryStats(
            distanceM = state.distanceM,
            movingMs = state.movingMs,
            averagePaceSecPerKm = state.averagePaceSecPerKm,
            calories = state.calories,
            unit = state.distanceUnit,
        )
        TimesCard(state)
        PaceChart(state.splits)
        if (state.splits.isNotEmpty()) RunSplitTable(state.splits)
        state.memo?.let { memo ->
            RunSection(title = stringResource(R.string.record_detail_memo)) {
                Text(
                    text = memo,
                    style = AppThemeTokens.typography.bodyMd,
                    color = AppThemeTokens.colors.textPrimary,
                    modifier = Modifier.testTag("record_detail_memo"),
                )
            }
        }
    }
}

// 미리보기: 샘플 시나리오(product-spec 9장) 10.28 (수) 오전 6:40 기록
@Suppress("MagicNumber")
private val PREVIEW_STATE =
    RecordDetailState(
        loading = false,
        title = null,
        defaultTitle = DefaultRunTitle(DayOfWeek.WEDNESDAY, TimeOfDay.MORNING),
        startedAt = 1_793_137_200_000L,
        endedAt = 1_793_139_090_000L,
        zoneId = "Asia/Seoul",
        distanceM = 5_210.0,
        movingMs = 1_812_000L,
        elapsedMs = 1_890_000L,
        averagePaceSecPerKm = 347.8,
        calories = 351,
        splits =
            listOf(
                SplitRow(1, 1_000.0, 352.0),
                SplitRow(2, 2_000.0, 344.0),
                SplitRow(3, 3_000.0, 340.0),
                SplitRow(4, 4_000.0, 350.0),
                SplitRow(5, 5_000.0, 347.0),
                SplitRow(6, 5_210.0, 376.0),
            ),
        memo = "한강 바람 시원했음",
    )

@Composable
private fun Preview(
    state: RecordDetailState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { RecordDetailScreen(state, {}) }
}

@ScreenPreviews
@Composable
private fun RecordDetailPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun RecordDetailInkDarkPreview() =
    Preview(PREVIEW_STATE.copy(title = "한강 아침 러닝"), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun RecordDetailMissingPreview() = Preview(RecordDetailState(loading = false, missing = true))
