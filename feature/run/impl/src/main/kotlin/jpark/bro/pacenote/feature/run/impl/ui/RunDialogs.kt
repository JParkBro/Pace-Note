package jpark.bro.pacenote.feature.run.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunBottomSheet
import jpark.bro.pacenote.core.designsystem.component.RunButtonSize
import jpark.bro.pacenote.core.designsystem.component.RunDangerTextButton
import jpark.bro.pacenote.core.designsystem.component.RunOutlineButton
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.tracking.RecoverableRun
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.run.impl.R
import jpark.bro.pacenote.feature.run.impl.RunAction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 거리 100m 미만이면 저장하지 않는 쪽을 기본으로 둔다 (product-spec 2-4) */
private const val MIN_SAVE_DISTANCE_M = 100.0

private val STARTED_AT_FORMAT = DateTimeFormatter.ofPattern("M.d (E) a h:mm", Locale.KOREAN)

/** P5 종료 확인 */
@Composable
internal fun RunFinishSheet(
    distanceM: Double,
    movingMs: Long,
    unit: DistanceUnit,
    onAction: (RunAction) -> Unit,
) {
    RunBottomSheet(onDismissRequest = { onAction(RunAction.FinishSheetDismissed) }) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.run_finish_title),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text =
                    stringResource(
                        R.string.run_finish_summary,
                        RunFormat.distance(distanceM, unit),
                        stringResource(RunFormat.unitLabelRes(unit)),
                        RunFormat.duration(movingMs),
                    ),
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            if (distanceM < MIN_SAVE_DISTANCE_M) {
                RunPrimaryButton(
                    stringResource(R.string.run_finish_discard),
                    { onAction(RunAction.DiscardClicked) },
                    Modifier.fillMaxWidth().testTag("run_finish_discard"),
                )
                RunSecondaryButton(stringResource(R.string.run_finish_continue), {
                    onAction(RunAction.ContinueClicked)
                }, Modifier.fillMaxWidth())
                RunOutlineButton(
                    stringResource(R.string.run_finish_save_short),
                    { onAction(RunAction.SaveClicked) },
                    Modifier.fillMaxWidth().testTag("run_finish_save"),
                    size = RunButtonSize.SMALL,
                )
            } else {
                RunPrimaryButton(
                    stringResource(R.string.run_finish_save),
                    { onAction(RunAction.SaveClicked) },
                    Modifier.fillMaxWidth().testTag("run_finish_save"),
                )
                RunSecondaryButton(stringResource(R.string.run_finish_continue), {
                    onAction(RunAction.ContinueClicked)
                }, Modifier.fillMaxWidth())
                RunDangerTextButton(
                    stringResource(R.string.run_finish_discard),
                    { onAction(RunAction.DiscardClicked) },
                    Modifier.fillMaxWidth().testTag("run_finish_discard"),
                )
            }
        }
    }
}

/** P8 복구. 24시간 넘게 방치된 기록이면 "저장하고 마치기"가 기본 */
@Composable
internal fun RunRecoverDialog(
    run: RecoverableRun,
    isOld: Boolean,
    unit: DistanceUnit,
    zoneId: ZoneId,
    onAction: (RunAction) -> Unit,
) {
    val startedAt = STARTED_AT_FORMAT.format(Instant.ofEpochMilli(run.startedAt).atZone(zoneId))
    AlertDialog(
        // 결정 없이 닫지 않는다. 뒤로 가기는 화면이 처리한다
        onDismissRequest = {},
        title = { Text(stringResource(R.string.run_recover_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isOld) Text(stringResource(R.string.run_recover_old), color = AppThemeTokens.colors.warning)
                Text(
                    stringResource(
                        R.string.run_recover_summary,
                        startedAt,
                        RunFormat.distance(run.distanceM, unit),
                        stringResource(RunFormat.unitLabelRes(unit)),
                        RunFormat.duration(run.movingMs),
                    ),
                )
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val continueButton: @Composable () -> Unit = {
                    RunButtonFor(isPrimary = !isOld, R.string.run_recover_continue, "run_recover_continue") {
                        onAction(RunAction.RecoverContinueClicked)
                    }
                }
                val saveButton: @Composable () -> Unit = {
                    RunButtonFor(isPrimary = isOld, R.string.run_recover_save, "run_recover_save") {
                        onAction(RunAction.RecoverSaveClicked)
                    }
                }
                if (isOld) saveButton() else continueButton()
                if (isOld) continueButton() else saveButton()
                RunDangerTextButton(
                    stringResource(R.string.run_recover_discard),
                    { onAction(RunAction.RecoverDiscardClicked) },
                    Modifier.fillMaxWidth().testTag("run_recover_discard"),
                )
            }
        },
    )
}

@Composable
private fun RunButtonFor(
    isPrimary: Boolean,
    text: Int,
    tag: String,
    onClick: () -> Unit,
) {
    val modifier = Modifier.fillMaxWidth().testTag(tag)
    if (isPrimary) {
        RunPrimaryButton(stringResource(text), onClick, modifier)
    } else {
        RunSecondaryButton(stringResource(text), onClick, modifier)
    }
}
