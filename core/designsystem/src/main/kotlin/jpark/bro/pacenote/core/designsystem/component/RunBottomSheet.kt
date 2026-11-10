package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/**
 * 바텀시트: surface-raised, 뒤 배경 overlay, 드래그 핸들 border-strong. 항상 끝까지 펼친다.
 * 내용에는 좌우 · 아래 여백(lg)을 시트가 준다. 호출하는 쪽에서 같은 여백을 다시 주지 않는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeTokens.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceRaised,
        scrimColor = colors.overlay,
        dragHandle = { RunSheetHandle() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg)
                    .padding(bottom = AppSpacing.lg),
            content = content,
        )
    }
}

/** 시트 위쪽 드래그 핸들 */
@Composable
fun RunSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(vertical = AppSpacing.sm + AppSpacing.xs)
                .size(width = 36.dp, height = 4.dp)
                .clip(AppShapes.full)
                .background(AppThemeTokens.colors.borderStrong),
    )
}

/** ModalBottomSheet는 Preview에서 그려지지 않으므로 같은 색 · 핸들로 시트 모양만 보여준다 */
@ComponentPreviews
@Composable
private fun RunBottomSheetPreview() {
    RunPreviewSurface {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.xl)
                    .background(AppThemeTokens.colors.surfaceRaised)
                    .padding(horizontal = AppSpacing.lg)
                    .padding(bottom = AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            RunSheetHandle()
            Text(
                text = "러닝을 마칠까요?",
                style = AppThemeTokens.typography.headlineMd,
                color = AppThemeTokens.colors.textPrimary,
            )
            RunPrimaryButton(text = "저장하고 마치기", onClick = {}, modifier = Modifier.fillMaxWidth())
            RunSecondaryButton(text = "계속 달리기", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
