package jpark.bro.pacenote.update

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.R
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton

/** NavDisplay 위에 띄우는 업데이트 다이얼로그. 어느 화면에 있든 뜬다 */
@Composable
internal fun UpdateDialogs(
    dialog: UpdateDialog,
    onAction: (UpdateAction) -> Unit,
) {
    when (dialog) {
        is UpdateDialog.Available -> {
            UpdateConfirmDialog(
                title = R.string.app_update_available_title,
                message = R.string.app_update_available_message,
                confirm = R.string.app_update_available_confirm,
                onConfirm = { onAction(UpdateAction.UpdateConfirmed) },
                onDismiss = { onAction(UpdateAction.UpdateDismissed) },
                testTag = "app_update_available",
            )
        }

        UpdateDialog.ReadyToInstall -> {
            UpdateConfirmDialog(
                title = R.string.app_update_ready_title,
                message = R.string.app_update_ready_message,
                confirm = R.string.app_update_ready_confirm,
                onConfirm = { onAction(UpdateAction.InstallConfirmed) },
                onDismiss = { onAction(UpdateAction.InstallDismissed) },
                testTag = "app_update_ready",
            )
        }

        UpdateDialog.None -> {
            Unit
        }
    }
}

@Composable
private fun UpdateConfirmDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes confirm: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    testTag: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(testTag),
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RunPrimaryButton(stringResource(confirm), onConfirm, Modifier.fillMaxWidth())
                RunSecondaryButton(stringResource(R.string.app_update_later), onDismiss, Modifier.fillMaxWidth())
            }
        },
    )
}
