package jpark.bro.pacenote.feature.settings.impl.profile

import androidx.activity.compose.BackHandler
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
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
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
import jpark.bro.pacenote.feature.settings.impl.R
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 내 정보 수정(M4)의 Root */
@Composable
internal fun ProfileEditRoot(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.settings_error)
    BackHandler { viewModel.onAction(ProfileEditAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ProfileEditEvent.Close -> onClose()
            ProfileEditEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    ProfileEditScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun ProfileEditScreen(
    state: ProfileEditState,
    snackbarHostState: SnackbarHostState,
    onAction: (ProfileEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.settings_profile_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.settings_back),
                        onClick = { onAction(ProfileEditAction.BackClicked) },
                    )
                },
            )
        },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            RunSection(title = stringResource(R.string.settings_profile_weight)) {
                RunTextField(
                    value = state.weightText,
                    onValueChange = { onAction(ProfileEditAction.WeightChanged(it)) },
                    placeholder = stringResource(R.string.settings_profile_weight_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("settings_profile_weight"),
                )
                if (state.weightInvalid) {
                    Text(
                        text = stringResource(R.string.settings_profile_weight_invalid),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.danger,
                        modifier = Modifier.testTag("settings_profile_weight_invalid"),
                    )
                }
            }
            RunSection(title = stringResource(R.string.settings_profile_unit)) {
                RunSegmentedControl(
                    segments =
                        listOf(
                            RunSegment(stringResource(R.string.settings_profile_unit_km)),
                            RunSegment(stringResource(R.string.settings_profile_unit_mile)),
                        ),
                    selectedIndex = state.distanceUnit.ordinal,
                    onSelect = { onAction(ProfileEditAction.UnitSelected(DistanceUnit.entries[it])) },
                    modifier = Modifier.testTag("settings_profile_unit"),
                )
            }
            Spacer(Modifier.weight(1f))
            RunPrimaryButton(
                text = stringResource(R.string.settings_save),
                onClick = { onAction(ProfileEditAction.SaveClicked) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().testTag("settings_profile_save"),
            )
        }
    }
}

@Composable
private fun Preview(
    state: ProfileEditState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { ProfileEditScreen(state, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun ProfileEditPreview() = Preview(ProfileEditState(loading = false, weightText = "65"))

@ScreenPreviews
@Composable
private fun ProfileEditInvalidPreview() =
    Preview(
        ProfileEditState(loading = false, weightText = "650", weightInvalid = true),
        AppThemeType.INK,
        AppThemeMode.DARK,
    )
