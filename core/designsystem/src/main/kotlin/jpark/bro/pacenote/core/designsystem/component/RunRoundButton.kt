package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppColors
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface
import kotlinx.coroutines.launch

private const val LONG_PRESS_MS = 1_000
private const val RING_START_DEGREES = -90f
private const val FULL_CIRCLE_DEGREES = 360f

enum class RunRoundButtonKind(
    internal val buttonSize: Dp,
    internal val iconSize: Dp,
) {
    /** 일시정지 · 재개: primary */
    PRIMARY(buttonSize = 72.dp, iconSize = 32.dp),

    /** 종료: fill */
    NEUTRAL(buttonSize = 72.dp, iconSize = 32.dp),

    /** 잠금: fill + text-secondary */
    LOCK(buttonSize = 56.dp, iconSize = 24.dp),
}

/**
 * 러닝 중 원형 버튼. 뛰면서 누르므로 터치 영역이 크다 (theme-guide.md 10장).
 * [longPress]면 1초 동안 누르고 있어야 [onClick]이 불리고, 누르는 동안 진행 링을 그린다.
 * 접근성 서비스에서는 길게 누르지 않아도 실행할 수 있다.
 */
@Composable
fun RunRoundButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: RunRoundButtonKind = RunRoundButtonKind.PRIMARY,
    longPress: Boolean = false,
) {
    val colors = AppThemeTokens.colors
    val style = colors.roundButtonStyle(kind)
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val latestOnClick by rememberUpdatedState(onClick)
    val input =
        if (longPress) {
            Modifier
                .semantics {
                    role = Role.Button
                    onClick {
                        latestOnClick()
                        true
                    }
                }.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        val hold =
                            scope.launch {
                                progress.animateTo(1f, tween(LONG_PRESS_MS, easing = LinearEasing))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                latestOnClick()
                                progress.snapTo(0f)
                            }
                        waitForUpOrCancellation()
                        if (hold.isActive) {
                            hold.cancel()
                            scope.launch { progress.animateTo(0f) }
                        }
                    }
                }
        } else {
            Modifier.clickable(role = Role.Button, onClick = onClick)
        }
    Box(
        modifier =
            modifier
                .size(kind.buttonSize)
                .then(
                    if (kind == RunRoundButtonKind.PRIMARY) {
                        Modifier.shadow(8.dp, AppShapes.full, ambientColor = colors.shadow, spotColor = colors.shadow)
                    } else {
                        Modifier
                    },
                ).clip(AppShapes.full)
                .background(style.container)
                .then(input),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = style.content,
            modifier = Modifier.size(kind.iconSize),
        )
        if (longPress) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 4.dp.toPx()
                drawArc(
                    color = style.ring,
                    startAngle = RING_START_DEGREES,
                    sweepAngle = FULL_CIRCLE_DEGREES * progress.value,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
    }
}

private data class RoundButtonStyle(
    val container: Color,
    val content: Color,
    val ring: Color,
)

private fun AppColors.roundButtonStyle(kind: RunRoundButtonKind): RoundButtonStyle =
    when (kind) {
        RunRoundButtonKind.PRIMARY -> RoundButtonStyle(primary, onPrimary, onPrimary)
        RunRoundButtonKind.NEUTRAL -> RoundButtonStyle(fill, textPrimary, danger)
        RunRoundButtonKind.LOCK -> RoundButtonStyle(fill, textSecondary, textPrimary)
    }

@ComponentPreviews
@Composable
private fun RunRoundButtonPreview() {
    RunPreviewSurface {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RunRoundButton(R.drawable.ic_pause, "일시정지", {})
            RunRoundButton(R.drawable.ic_stop, "길게 눌러 종료", {}, kind = RunRoundButtonKind.NEUTRAL, longPress = true)
            RunRoundButton(R.drawable.ic_lock_open, "길게 눌러 잠금 해제", {}, kind = RunRoundButtonKind.LOCK, longPress = true)
        }
    }
}
