package jpark.bro.pacenote.feature.home.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.ui.ObserveEvents
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import jpark.bro.pacenote.core.designsystem.R as DesignR

private val WEEK_DAY_FORMAT = DateTimeFormatter.ofPattern("M.d")
private const val DAYS_TO_SUNDAY = 6L

/** 홈(H1)의 Root: ViewModel 연결과 Event 처리 */
@Composable
internal fun HomeRoot(
    onStartRun: () -> Unit,
    onOpenRun: (runId: String) -> Unit,
    viewModel: HomeViewModel,
    debugMenu: HomeDebugMenu? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            HomeEvent.StartRun -> onStartRun()
            is HomeEvent.OpenRun -> onOpenRun(event.runId)
        }
    }
    HomeScreen(state, viewModel::onAction, debugMenu = debugMenu)
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    debugMenu: HomeDebugMenu? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeTopBar(state) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            if (!state.loading) WeekCard(state)
            RunPrimaryButton(
                text = stringResource(R.string.home_start_run),
                onClick = { onAction(HomeAction.StartRunClicked) },
                icon = DesignR.drawable.ic_play_arrow,
                modifier = Modifier.fillMaxWidth().testTag("home_start_run"),
            )
            if (!state.loading) RecentRuns(state.recentRuns, state.distanceUnit, onAction)
            debugMenu?.Content()
        }
    }
}

/** "이번 주" + 기간(`10.26 – 11.1`) */
@Composable
private fun HomeTopBar(state: HomeState) {
    RunTopBar(
        title = stringResource(R.string.home_title),
        actions = {
            if (!state.loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text =
                            stringResource(
                                R.string.home_week_range,
                                WEEK_DAY_FORMAT.format(state.weekStart),
                                WEEK_DAY_FORMAT.format(state.weekStart.plusDays(DAYS_TO_SUNDAY)),
                            ),
                        style = AppThemeTokens.typography.bodyMd,
                        color = AppThemeTokens.colors.textSecondary,
                        modifier = Modifier.testTag("home_week_range"),
                    )
                }
            }
        },
    )
}

// 미리보기: 샘플 시나리오(product-spec 9장). 오늘 10.29 (목), 이번 주 3회 12.4km, 목표 15km
private const val PREVIEW_ROUTE_POINTS = 30

@Suppress("MagicNumber")
private val PREVIEW_WEEK_START: LocalDate = LocalDate.of(2026, 10, 26)

@Suppress("MagicNumber")
private val PREVIEW_ROUTE =
    List(PREVIEW_ROUTE_POINTS) { i ->
        val t = i * 2 * PI / (PREVIEW_ROUTE_POINTS - 1)
        RoutePoint(37.52 + 0.003 * sin(t), 126.93 + 0.004 * cos(t), 0)
    }

@Suppress("MagicNumber")
private fun previewState(goal: HomeGoal? = HomeGoal(GoalType.DISTANCE, 15_000.0, 0.8267)) =
    HomeState(
        loading = false,
        weekStart = PREVIEW_WEEK_START,
        distanceM = 12_400.0,
        movingMs = 4_360_000L,
        runCount = 3,
        averagePaceSecPerKm = 351.6,
        goal = goal,
        recentRuns =
            listOf(
                HomeRun("run-1028", 1_793_137_200_000L, "Asia/Seoul", 5_210.0, 1_812_000L, 347.8, PREVIEW_ROUTE),
                HomeRun("run-1026", 1_792_967_400_000L, "Asia/Seoul", 3_100.0, 1_116_000L, 360.0, PREVIEW_ROUTE),
            ),
    )

@Composable
private fun Preview(
    state: HomeState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { HomeScreen(state, {}) }
}

@ScreenPreviews
@Composable
private fun HomePreview() = Preview(previewState())

@ScreenPreviews
@Composable
private fun HomeInkDarkPreview() = Preview(previewState(), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun HomeGoalAchievedPreview() = Preview(previewState(HomeGoal(GoalType.COUNT, 3.0, 1.0)))

@ScreenPreviews
@Composable
private fun HomeEmptyPreview() = Preview(HomeState(loading = false, weekStart = PREVIEW_WEEK_START))
