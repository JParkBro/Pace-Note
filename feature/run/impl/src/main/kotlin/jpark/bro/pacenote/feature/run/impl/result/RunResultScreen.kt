package jpark.bro.pacenote.feature.run.impl.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.run.api.RunResult
import jpark.bro.pacenote.feature.run.impl.R
import jpark.bro.pacenote.feature.run.impl.ui.RunLoadingContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

private val STARTED_AT_FORMAT = DateTimeFormatter.ofPattern("M월 d일 a h:mm", Locale.KOREAN)

/** 결과(P6)의 Root: ViewModel 연결, 뒤로 가기("완료"와 같음), Event 처리 */
@Composable
internal fun RunResultRoot(
    onFinished: (RunResult) -> Unit,
    viewModel: RunResultViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.run_error)

    BackHandler { viewModel.onAction(RunResultAction.DoneClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            RunResultEvent.Finish -> onFinished(RunResult.Finished)
            RunResultEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    RunResultScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun RunResultScreen(
    state: RunResultState,
    snackbarHostState: SnackbarHostState,
    onAction: (RunResultAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        if (state.loading) {
            ""
        } else {
            STARTED_AT_FORMAT.format(Instant.ofEpochMilli(state.startedAt).atZone(ZoneId.of(state.zoneId)))
        }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = title,
                actions = {
                    TextButton(
                        onClick = { onAction(RunResultAction.DoneClicked) },
                        modifier = Modifier.testTag("run_result_done"),
                    ) {
                        Text(
                            text = stringResource(R.string.run_result_done),
                            style = AppThemeTokens.typography.labelLg,
                            color = AppThemeTokens.colors.textSecondary,
                        )
                    }
                },
            )
        },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) {
            RunLoadingContent(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
        ) {
            ResultRoute(state.route)
            ResultStats(state)
            state.weeklyGoal?.let { ResultGoalCard(it, state.distanceUnit) }
            if (state.splits.isNotEmpty()) ResultSplits(state.splits)
            ResultMemo(state.memo) { onAction(RunResultAction.MemoChanged(it)) }
        }
    }
}

// 미리보기: 샘플 시나리오(product-spec 9장) 10.28 (수) 오전 6:40, 5.21km, 30:12, 351 kcal
private const val PREVIEW_STARTED_AT = 1_793_137_200_000L
private const val PREVIEW_ROUTE_POINTS = 40

@Suppress("MagicNumber")
private val PREVIEW_SPLITS =
    listOf(
        ResultSplit(1, 1_000.0, 352.0),
        ResultSplit(2, 2_000.0, 344.0),
        ResultSplit(3, 3_000.0, 340.0),
        ResultSplit(4, 4_000.0, 350.0),
        ResultSplit(5, 5_000.0, 347.0),
        ResultSplit(6, 5_210.0, 340.0),
    )

/** 동서로 2km쯤 가며 남북으로 한 번 굽이치는 경로 */
@Suppress("MagicNumber")
private val PREVIEW_ROUTE =
    List(PREVIEW_ROUTE_POINTS) { i ->
        val t = i / (PREVIEW_ROUTE_POINTS - 1.0)
        RoutePoint(37.52 + 0.004 * sin(t * 2 * PI), 126.93 + 0.02 * t, 0)
    }

@Composable
private fun Preview(
    state: RunResultState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) {
        RunResultScreen(state, remember { SnackbarHostState() }, {})
    }
}

@Suppress("MagicNumber")
private fun previewState(goal: ResultGoal? = ResultGoal(GoalType.DISTANCE, 0.83, 2_600.0)) =
    RunResultState(
        loading = false,
        startedAt = PREVIEW_STARTED_AT,
        zoneId = "Asia/Seoul",
        distanceM = 5_210.0,
        movingMs = 1_812_000L,
        averagePaceSecPerKm = 347.8,
        calories = 351,
        route = PREVIEW_ROUTE,
        splits = PREVIEW_SPLITS,
        weeklyGoal = goal,
        memo = "한강 바람 시원했음",
    )

@ScreenPreviews
@Composable
private fun RunResultPreview() = Preview(previewState())

@ScreenPreviews
@Composable
private fun RunResultInkDarkPreview() = Preview(previewState(), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun RunResultNoGoalPreview() = Preview(previewState(goal = null).copy(memo = "", calories = null))
