package jpark.bro.pacenote.feature.onboarding.impl.goal

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
import androidx.compose.material3.TextButton
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
import jpark.bro.pacenote.feature.onboarding.impl.R
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 첫 목표(O3)의 Root */
@Composable
internal fun FirstGoalRoot(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: FirstGoalViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.onboarding_error)
    BackHandler { viewModel.onAction(FirstGoalAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            FirstGoalEvent.Done -> onDone()
            FirstGoalEvent.NavigateBack -> onBack()
            FirstGoalEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    FirstGoalScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun FirstGoalScreen(
    state: FirstGoalState,
    snackbarHostState: SnackbarHostState,
    onAction: (FirstGoalAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.onboarding_goal_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.onboarding_back),
                        onClick = { onAction(FirstGoalAction.BackClicked) },
                    )
                },
                actions = {
                    TextButton(
                        onClick = { onAction(FirstGoalAction.SkipClicked) },
                        enabled = !state.working,
                        modifier = Modifier.testTag("onboarding_goal_skip"),
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            style = AppThemeTokens.typography.labelLg,
                            color = AppThemeTokens.colors.textSecondary,
                        )
                    }
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
                        RunSegment(stringResource(R.string.onboarding_goal_distance)),
                        RunSegment(stringResource(R.string.onboarding_goal_count)),
                    ),
                selectedIndex = state.type.ordinal,
                onSelect = { onAction(FirstGoalAction.TypeSelected(GoalType.entries[it])) },
                modifier = Modifier.testTag("onboarding_goal_type"),
            )
            Spacer(Modifier.weight(1f))
            RunStepper(
                decreaseDescription = stringResource(R.string.onboarding_goal_decrease),
                increaseDescription = stringResource(R.string.onboarding_goal_increase),
                onDecrease = { onAction(FirstGoalAction.DecreaseClicked) },
                onIncrease = { onAction(FirstGoalAction.IncreaseClicked) },
                decreaseEnabled = state.canDecrease,
                increaseEnabled = state.canIncrease,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_goal_stepper"),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = state.amount.toString(),
                        style = AppThemeTokens.typography.metricHuge,
                        color = AppThemeTokens.colors.textPrimary,
                    )
                    Text(
                        text =
                            when (state.type) {
                                GoalType.DISTANCE -> stringResource(RunFormat.unitLabelRes(state.distanceUnit))
                                GoalType.COUNT -> stringResource(R.string.onboarding_goal_count_unit)
                            },
                        style = AppThemeTokens.typography.headlineSm,
                        color = AppThemeTokens.colors.textSecondary,
                        modifier = Modifier.padding(start = AppSpacing.xs, bottom = AppSpacing.sm),
                    )
                }
            }
            Text(
                text = stringResource(R.string.onboarding_goal_body),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
            )
            Spacer(Modifier.weight(1f))
            RunPrimaryButton(
                text = stringResource(R.string.onboarding_goal_start),
                onClick = { onAction(FirstGoalAction.StartClicked) },
                enabled = !state.working,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_goal_start"),
            )
        }
    }
}

@Composable
private fun Preview(
    state: FirstGoalState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { FirstGoalScreen(state, remember { SnackbarHostState() }, {}) }
}

@Suppress("MagicNumber")
private val PREVIEW_STATE = FirstGoalState(loading = false, amount = 15)

@ScreenPreviews
@Composable
private fun FirstGoalPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun FirstGoalInkDarkPreview() =
    Preview(PREVIEW_STATE.copy(type = GoalType.COUNT, amount = 3), AppThemeType.INK, AppThemeMode.DARK)
