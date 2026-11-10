package jpark.bro.pacenote.feature.settings.impl.goal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSegment
import jpark.bro.pacenote.core.designsystem.component.RunSegmentedControl
import jpark.bro.pacenote.core.designsystem.component.RunStepper
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.settings.impl.R
import java.util.Locale
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 목표(M2)의 Root: ViewModel 연결, 뒤로 가기, Event 처리 */
@Composable
internal fun GoalRoot(
    onClose: () -> Unit,
    viewModel: GoalViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.settings_error)
    BackHandler { viewModel.onAction(GoalAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            GoalEvent.Close -> onClose()
            GoalEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    GoalScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun GoalScreen(
    state: GoalState,
    snackbarHostState: SnackbarHostState,
    onAction: (GoalAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.settings_goal_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.settings_back),
                        onClick = { onAction(GoalAction.BackClicked) },
                    )
                },
            )
        },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(AppSpacing.marginMobile),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            RunSegmentedControl(
                segments =
                    listOf(
                        RunSegment(stringResource(R.string.settings_goal_distance)),
                        RunSegment(stringResource(R.string.settings_goal_count)),
                    ),
                selectedIndex = state.type.ordinal,
                onSelect = { onAction(GoalAction.TypeSelected(GoalType.entries[it])) },
                modifier = Modifier.testTag("settings_goal_type"),
            )
            Spacer(Modifier.weight(1f))
            GoalStepper(state, onAction)
            recentText(state)?.let {
                Text(
                    text = it,
                    style = AppThemeTokens.typography.bodyMd,
                    color = AppThemeTokens.colors.textSecondary,
                    modifier = Modifier.testTag("settings_goal_recent"),
                )
            }
            Spacer(Modifier.weight(1f))
            RunPrimaryButton(
                text = stringResource(R.string.settings_save),
                onClick = { onAction(GoalAction.SaveClicked) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().testTag("settings_goal_save"),
            )
        }
    }
}

@Composable
private fun GoalStepper(
    state: GoalState,
    onAction: (GoalAction) -> Unit,
) {
    RunStepper(
        decreaseDescription = stringResource(R.string.settings_goal_decrease),
        increaseDescription = stringResource(R.string.settings_goal_increase),
        onDecrease = { onAction(GoalAction.DecreaseClicked) },
        onIncrease = { onAction(GoalAction.IncreaseClicked) },
        decreaseEnabled = state.canDecrease,
        increaseEnabled = state.canIncrease,
        modifier = Modifier.fillMaxWidth().testTag("settings_goal_stepper"),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.amount.toString(),
                style = AppThemeTokens.typography.metricHuge,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.testTag("settings_goal_amount"),
            )
            Text(
                text =
                    when (state.type) {
                        GoalType.DISTANCE -> stringResource(RunFormat.unitLabelRes(state.distanceUnit))
                        GoalType.COUNT -> stringResource(R.string.settings_goal_count_unit)
                    },
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textSecondary,
                modifier = Modifier.padding(start = AppSpacing.xs, bottom = AppSpacing.sm),
            )
        }
    }
}

/** "최근 4주 평균 12.80km" · "최근 4주 평균 2.5회" */
@Composable
private fun recentText(state: GoalState): String? =
    when (state.type) {
        GoalType.DISTANCE -> {
            state.recentDistanceM?.let {
                stringResource(
                    R.string.settings_goal_average_distance,
                    RunFormat.distance(it, state.distanceUnit),
                    stringResource(RunFormat.unitLabelRes(state.distanceUnit)),
                )
            }
        }

        GoalType.COUNT -> {
            state.recentCount?.let {
                stringResource(R.string.settings_goal_average_count, String.format(Locale.KOREAN, "%.1f", it))
            }
        }
    }

@Suppress("MagicNumber")
private val PREVIEW_STATE = GoalState(loading = false, amount = 15, recentDistanceM = 12_800.0, recentCount = 3.0)

@Composable
private fun Preview(
    state: GoalState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { GoalScreen(state, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun GoalPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun GoalInkDarkPreview() =
    Preview(PREVIEW_STATE.copy(type = GoalType.COUNT, amount = 3), AppThemeType.INK, AppThemeMode.DARK)
