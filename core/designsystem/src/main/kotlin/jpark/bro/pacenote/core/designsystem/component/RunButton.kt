package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 주 버튼 (primary, 눌림 primary-pressed). 화면당 한 곳만 */
@Composable
fun RunPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RunButtonSize = RunButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    RunButton(
        text = text,
        onClick = onClick,
        container = if (pressed) colors.primaryPressed else colors.primary,
        content = colors.onPrimary,
        modifier = modifier,
        enabled = enabled,
        size = size,
        icon = icon,
        interactionSource = interactionSource,
    )
}

/** 보조 버튼 (취소 등): fill */
@Composable
fun RunSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RunButtonSize = RunButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    RunButton(text, onClick, colors.fill, colors.textPrimary, modifier, enabled, size, icon)
}

/** 아웃라인 버튼: 투명 + border-strong */
@Composable
fun RunOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RunButtonSize = RunButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
) {
    val colors = AppThemeTokens.colors
    RunButton(
        text = text,
        onClick = onClick,
        container = Color.Transparent,
        content = colors.textPrimary,
        modifier = modifier,
        enabled = enabled,
        size = size,
        icon = icon,
        border = BorderStroke(1.dp, colors.borderStrong),
    )
}

/** 위험 텍스트 버튼 (삭제 · 탈퇴): 투명 배경 + danger. 되돌리기 어려운 동작은 화면 맨 아래에 둔다 */
@Composable
fun RunDangerTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RunButtonSize = RunButtonSize.SMALL,
) {
    RunButton(text, onClick, Color.Transparent, AppThemeTokens.colors.danger, modifier, enabled, size)
}

/** 모든 버튼의 공통 모양: 모서리 lg · 최소 높이(글꼴이 커지면 늘어남) · 비활성 fill + text-tertiary */
@Composable
private fun RunButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RunButtonSize = RunButtonSize.LARGE,
    @DrawableRes icon: Int? = null,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = AppThemeTokens.colors
    val typography = AppThemeTokens.typography
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = size.minHeight),
        enabled = enabled,
        shape = AppShapes.lg,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = container,
                contentColor = content,
                disabledContainerColor = colors.fill,
                disabledContentColor = colors.textTertiary,
            ),
        border = border.takeIf { enabled },
        contentPadding = PaddingValues(horizontal = size.horizontalPadding, vertical = AppSpacing.sm),
        interactionSource = interactionSource,
    ) {
        Row {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize),
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
            }
            Text(
                text = text,
                style = if (size == RunButtonSize.LARGE) typography.labelLg else typography.labelMd,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@ComponentPreviews
@Composable
private fun RunButtonPreview() {
    RunPreviewSurface {
        val full = Modifier.fillMaxWidth()
        RunPrimaryButton(text = "달리기 시작", onClick = {}, modifier = full)
        RunPrimaryButton(text = "시작", onClick = {}, modifier = full, enabled = false)
        RunSecondaryButton(text = "다음에 하기", onClick = {}, modifier = full)
        RunOutlineButton(text = "로그인 없이 시작", onClick = {}, modifier = full)
        RunDangerTextButton(text = "이 기록 삭제", onClick = {}, modifier = full)
        Row {
            RunPrimaryButton(text = "저장", onClick = {}, size = RunButtonSize.SMALL, icon = R.drawable.ic_check)
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            RunSecondaryButton(text = "닫기", onClick = {}, size = RunButtonSize.SMALL)
        }
    }
}
