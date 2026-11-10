package jpark.bro.pacenote.feature.record.impl.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import jpark.bro.pacenote.core.designsystem.component.RunDangerTextButton
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunTextField
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle
import jpark.bro.pacenote.core.domain.stats.TimeOfDay
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.record.impl.R
import jpark.bro.pacenote.feature.record.impl.ui.defaultTitleText
import java.time.DayOfWeek
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 기록 편집(R4)의 Root: ViewModel 연결, 뒤로 가기, Event 처리 */
@Composable
internal fun RunEditRoot(
    onClose: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: RunEditViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.record_error)
    BackHandler { viewModel.onAction(RunEditAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            RunEditEvent.Close -> onClose()
            RunEditEvent.Deleted -> onDeleted()
            RunEditEvent.ShowError -> snackbarHostState.showSnackbar(errorMessage)
        }
    }
    RunEditScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
private fun RunEditScreen(
    state: RunEditState,
    snackbarHostState: SnackbarHostState,
    onAction: (RunEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { EditTopBar(state, onAction) },
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
            RunSection(title = stringResource(R.string.record_edit_name)) {
                RunTextField(
                    value = state.title,
                    onValueChange = { onAction(RunEditAction.TitleChanged(it)) },
                    placeholder = state.defaultTitle?.let { defaultTitleText(it) },
                    maxLength = RunEditState.TITLE_MAX_LENGTH,
                    modifier = Modifier.fillMaxWidth().testTag("record_edit_title"),
                )
            }
            RunSection(title = stringResource(R.string.record_edit_memo)) {
                RunTextField(
                    value = state.memo,
                    onValueChange = { onAction(RunEditAction.MemoChanged(it)) },
                    singleLine = false,
                    maxLength = RunEditState.MEMO_MAX_LENGTH,
                    modifier = Modifier.fillMaxWidth().testTag("record_edit_memo"),
                )
            }
            RunDangerTextButton(
                stringResource(R.string.record_edit_delete),
                { onAction(RunEditAction.DeleteClicked) },
                Modifier.fillMaxWidth().testTag("record_edit_delete"),
            )
        }
        if (state.showDeleteDialog) DeleteDialog(onAction)
    }
}

@Composable
private fun EditTopBar(
    state: RunEditState,
    onAction: (RunEditAction) -> Unit,
) {
    RunTopBar(
        title = stringResource(R.string.record_edit_title),
        navigationIcon = {
            RunIconButton(
                icon = DesignR.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.record_back),
                onClick = { onAction(RunEditAction.BackClicked) },
            )
        },
        actions = {
            TextButton(
                onClick = { onAction(RunEditAction.SaveClicked) },
                enabled = !state.loading && !state.working,
                modifier = Modifier.testTag("record_edit_save"),
            ) {
                Text(
                    text = stringResource(R.string.record_edit_save),
                    style = AppThemeTokens.typography.labelLg,
                    color = AppThemeTokens.colors.textPrimary,
                )
            }
        },
    )
}

@Composable
private fun DeleteDialog(onAction: (RunEditAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(RunEditAction.DeleteDismissed) },
        title = { Text(stringResource(R.string.record_edit_delete_title)) },
        text = { Text(stringResource(R.string.record_edit_delete_body)) },
        confirmButton = {
            RunDangerTextButton(
                stringResource(R.string.record_edit_delete_confirm),
                { onAction(RunEditAction.DeleteConfirmed) },
                Modifier.testTag("record_edit_delete_confirm"),
            )
        },
        dismissButton = {
            TextButton(onClick = { onAction(RunEditAction.DeleteDismissed) }) {
                Text(stringResource(R.string.record_edit_delete_cancel), color = AppThemeTokens.colors.textSecondary)
            }
        },
    )
}

private val PREVIEW_STATE =
    RunEditState(
        loading = false,
        title = "",
        memo = "한강 바람 시원했음",
        defaultTitle = DefaultRunTitle(DayOfWeek.WEDNESDAY, TimeOfDay.MORNING),
    )

@Composable
private fun Preview(
    state: RunEditState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { RunEditScreen(state, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun RunEditPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun RunEditInkDarkPreview() =
    Preview(PREVIEW_STATE.copy(title = "한강 아침 러닝"), AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun RunEditDeleteDialogPreview() = Preview(PREVIEW_STATE.copy(showDeleteDialog = true))
