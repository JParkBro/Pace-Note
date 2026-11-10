package jpark.bro.pacenote.feature.settings.impl.my

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunCard
import jpark.bro.pacenote.core.designsystem.component.RunListItem
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunStatTile
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.Goal
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.settings.impl.R
import java.time.LocalDate
import kotlin.math.roundToInt

/** 내 정보(M1)의 Root: ViewModel 연결과 Event 처리 */
@Composable
internal fun MyRoot(
    onOpenGoal: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRunSettings: () -> Unit,
    viewModel: MyViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName =
        remember {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                .orEmpty()
        }
    val snackbarHostState = remember { SnackbarHostState() }
    val noMailApp = stringResource(R.string.settings_contact_no_app)
    val errorMessage = stringResource(R.string.settings_error)
    val contactIntent = contactIntent(versionName)
    val ossTitle = stringResource(R.string.settings_my_oss_licenses)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            MyEvent.OpenContact -> {
                runCatching { context.startActivity(contactIntent) }
                    .onFailure { snackbarHostState.showSnackbar(noMailApp) }
            }

            MyEvent.OpenOssLicenses -> {
                OssLicensesMenuActivity.setActivityTitle(ossTitle)
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            }

            MyEvent.ShowError -> {
                snackbarHostState.showSnackbar(errorMessage)
            }

            MyEvent.OpenGoal -> {
                onOpenGoal()
            }

            MyEvent.OpenRunSettings -> {
                onOpenRunSettings()
            }

            MyEvent.OpenProfile -> {
                onOpenProfile()
            }
        }
    }
    MyScreen(state, versionName, snackbarHostState, viewModel::onAction)
}

@Composable
private fun MyScreen(
    state: MyState,
    versionName: String,
    snackbarHostState: SnackbarHostState,
    onAction: (MyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RunTopBar(title = stringResource(R.string.settings_my_title)) },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SummaryCard(state)
            RunSection(title = stringResource(R.string.settings_my_section_running)) {
                RunListItem(
                    title = stringResource(R.string.settings_my_goal),
                    subtitle = goalText(state.goal, state.distanceUnit),
                    onClick = { onAction(MyAction.GoalClicked) },
                    modifier = Modifier.testTag("settings_my_goal"),
                )
                RunListItem(
                    title = stringResource(R.string.settings_my_run_settings),
                    onClick = { onAction(MyAction.RunSettingsClicked) },
                    modifier = Modifier.testTag("settings_my_run_settings"),
                )
                RunListItem(
                    title = stringResource(R.string.settings_my_profile),
                    onClick = { onAction(MyAction.ProfileClicked) },
                    modifier = Modifier.testTag("settings_my_profile"),
                )
            }
            RunSection(title = stringResource(R.string.settings_my_section_app)) {
                RunListItem(
                    title = stringResource(R.string.settings_my_theme),
                    onClick = { onAction(MyAction.ThemeClicked) },
                    modifier = Modifier.testTag("settings_my_theme"),
                )
            }
            RunSection(title = stringResource(R.string.settings_my_section_support)) {
                RunListItem(
                    title = stringResource(R.string.settings_my_contact),
                    onClick = { onAction(MyAction.ContactClicked) },
                    modifier = Modifier.testTag("settings_my_contact"),
                )
            }
            RunSection(title = stringResource(R.string.settings_my_section_info)) {
                RunListItem(
                    title = stringResource(R.string.settings_my_oss_licenses),
                    onClick = { onAction(MyAction.OssLicensesClicked) },
                    modifier = Modifier.testTag("settings_my_oss_licenses"),
                )
                RunListItem(
                    title = stringResource(R.string.settings_my_font_license),
                    onClick = { onAction(MyAction.FontLicenseClicked) },
                    modifier = Modifier.testTag("settings_my_font_license"),
                )
            }
            Text(
                text = stringResource(R.string.settings_my_version, versionName),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("settings_my_version"),
            )
        }
        state.themeSheet?.let { ThemeSheet(it, onAction) }
        if (state.fontLicenseSheetVisible) FontLicenseSheet(onAction)
    }
}

/** 요약 카드: 누적 거리 · 누적 러닝 */
@Composable
private fun SummaryCard(state: MyState) {
    RunCard(modifier = Modifier.testTag("settings_my_summary")) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RunStatTile(
                value = RunFormat.distance(state.totalDistanceM, state.distanceUnit),
                label =
                    stringResource(
                        R.string.settings_my_total_distance,
                        stringResource(RunFormat.unitLabelRes(state.distanceUnit)),
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            RunStatTile(
                value = stringResource(R.string.settings_my_total_runs_value, state.totalRuns),
                label = stringResource(R.string.settings_my_total_runs),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
        }
    }
}

@Composable
private fun goalText(
    goal: Goal?,
    unit: DistanceUnit,
): String =
    when (goal?.type) {
        null -> {
            stringResource(R.string.settings_my_goal_none)
        }

        GoalType.DISTANCE -> {
            stringResource(
                R.string.settings_my_goal_distance,
                (goal.value / RunFormat.unitMeters(unit)).roundToInt().toString(),
                stringResource(RunFormat.unitLabelRes(unit)),
            )
        }

        GoalType.COUNT -> {
            stringResource(R.string.settings_my_goal_count, goal.value.roundToInt())
        }
    }

// 미리보기: 샘플 시나리오(product-spec 9장) 누적 312.5 km · 54회, 목표 주간 15km
@Suppress("MagicNumber")
private val PREVIEW_STATE =
    MyState(
        loading = false,
        totalDistanceM = 312_500.0,
        totalRuns = 54,
        goal = Goal("goal", null, GoalType.DISTANCE, 15_000.0, LocalDate.of(2026, 10, 26), 0, 0, null),
    )

@Composable
private fun Preview(
    state: MyState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { MyScreen(state, "1.0.0", remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun MyPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun MyInkDarkPreview() = Preview(PREVIEW_STATE.copy(goal = null), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun MyThemeSheetPreview() =
    Preview(PREVIEW_STATE.copy(themeSheet = ThemeSetting(AppThemeType.SUNRISE, AppThemeMode.DARK)))

@ScreenPreviews
@Composable
private fun MyFontLicenseSheetPreview() = Preview(PREVIEW_STATE.copy(fontLicenseSheetVisible = true))
