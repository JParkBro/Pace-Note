package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 원형 보조 아이콘 버튼 (스테퍼 · 시트 닫기 등): fill 배경 + text-primary 아이콘 */
@Composable
fun RunIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeTokens.colors
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(colors.fill),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = if (enabled) colors.textPrimary else colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** − 값 + 스테퍼. [value]는 호출하는 쪽이 만든 표시 내용이다 */
@Composable
fun RunStepper(
    decreaseDescription: String,
    increaseDescription: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    decreaseEnabled: Boolean = true,
    increaseEnabled: Boolean = true,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RunIconButton(R.drawable.ic_remove, decreaseDescription, onDecrease, enabled = decreaseEnabled)
        value()
        RunIconButton(R.drawable.ic_add, increaseDescription, onIncrease, enabled = increaseEnabled)
    }
}

@ComponentPreviews
@Composable
private fun RunIconButtonPreview() {
    RunPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            RunIconButton(icon = R.drawable.ic_close, contentDescription = "닫기", onClick = {})
            RunIconButton(icon = R.drawable.ic_close, contentDescription = "닫기", onClick = {}, enabled = false)
        }
        RunStepper(
            decreaseDescription = "줄이기",
            increaseDescription = "늘리기",
            onDecrease = {},
            onIncrease = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "15 km", style = AppThemeTokens.typography.metricLg, color = AppThemeTokens.colors.textPrimary)
        }
    }
}
