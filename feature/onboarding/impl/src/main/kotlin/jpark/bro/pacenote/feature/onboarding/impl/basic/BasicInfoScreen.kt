package jpark.bro.pacenote.feature.onboarding.impl.basic

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunSegment
import jpark.bro.pacenote.core.designsystem.component.RunSegmentedControl
import jpark.bro.pacenote.core.designsystem.component.RunTextField
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.onboarding.impl.R

/** 기본 정보(O2)의 Root */
@Composable
internal fun BasicInfoRoot(
    onNext: () -> Unit,
    viewModel: BasicInfoViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.onboarding_error)
    val activity = LocalActivity.current
    // 온보딩의 첫 화면: 뒤로 가면 아래의 홈으로 가지 않고 앱을 닫는다
    BackHandler { activity?.finish() }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            BasicInfoEvent.OpenGoal -> onNext()
            BasicInfoEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    BasicInfoScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun BasicInfoScreen(
    state: BasicInfoState,
    snackbarHostState: SnackbarHostState,
    onAction: (BasicInfoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.onboarding_basic_title),
                actions = {
                    TextButton(
                        onClick = { onAction(BasicInfoAction.SkipClicked) },
                        modifier = Modifier.testTag("onboarding_basic_skip"),
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
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            RunSection(title = stringResource(R.string.onboarding_basic_weight)) {
                RunTextField(
                    value = state.weightText,
                    onValueChange = { onAction(BasicInfoAction.WeightChanged(it)) },
                    placeholder = stringResource(R.string.onboarding_basic_weight_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_basic_weight"),
                )
                if (state.weightInvalid) {
                    Text(
                        text = stringResource(R.string.onboarding_basic_weight_invalid),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.danger,
                    )
                }
            }
            RunSection(title = stringResource(R.string.onboarding_basic_unit)) {
                RunSegmentedControl(
                    segments =
                        listOf(
                            RunSegment(stringResource(R.string.onboarding_basic_unit_km)),
                            RunSegment(stringResource(R.string.onboarding_basic_unit_mile)),
                        ),
                    selectedIndex = state.distanceUnit.ordinal,
                    onSelect = { onAction(BasicInfoAction.UnitSelected(DistanceUnit.entries[it])) },
                    modifier = Modifier.testTag("onboarding_basic_unit"),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.onboarding_basic_body),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
            )
            RunPrimaryButton(
                text = stringResource(R.string.onboarding_next),
                onClick = { onAction(BasicInfoAction.NextClicked) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().testTag("onboarding_basic_next"),
            )
        }
    }
}

@Composable
private fun Preview(
    state: BasicInfoState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { BasicInfoScreen(state, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun BasicInfoPreview() = Preview(BasicInfoState(weightText = "65"))

@ScreenPreviews
@Composable
private fun BasicInfoInkDarkPreview() =
    Preview(BasicInfoState(weightText = "650", weightInvalid = true), AppThemeType.INK, AppThemeMode.DARK)
