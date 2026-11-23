package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 마스코트 스프라이트 한 장의 픽셀 크기 (80×80, 투명 배경) */
private const val SPRITE_PX = 80

/** 통통 튀기 한 프레임 길이 */
private const val BOUNCE_FRAME_MS = 300

/** 정지 마스코트 (theme-guide.md 11장 "마스코트"). 색은 테마와 상관없이 마스코트 고유색이다. */
enum class RunMascotPose(
    @param:DrawableRes val res: Int,
) {
    IDLE(R.drawable.mascot_front_idle),
    WAVE(R.drawable.mascot_wave),
    JUMP(R.drawable.mascot_jump),
    WORRIED(R.drawable.mascot_face_worried),
}

/**
 * 정지 마스코트. 옆에 같은 뜻의 문구가 있으면 [contentDescription]은 null(장식).
 * [size]는 원하는 크기이고, 픽셀이 고르게 보이도록 그 안에서 가장 큰 정수 배율로 그린다.
 */
@Composable
fun RunMascot(
    pose: RunMascotPose,
    modifier: Modifier = Modifier,
    size: Dp = RunMascotDefaults.Size,
    contentDescription: String? = null,
) {
    MascotSprite(ImageBitmap.imageResource(pose.res), size, contentDescription, modifier)
}

/** 커스텀 로딩: 통통 튀기(f1 ↔ f2). `CircularProgressIndicator` 대신 쓴다. */
@Composable
fun RunMascotLoading(
    modifier: Modifier = Modifier,
    size: Dp = RunMascotDefaults.Size,
) {
    val frames =
        listOf(
            ImageBitmap.imageResource(R.drawable.mascot_bounce_f1),
            ImageBitmap.imageResource(R.drawable.mascot_bounce_f2),
        )
    // Preview · 스크린샷에서는 첫 프레임으로 고정한다
    val frame =
        if (LocalInspectionMode.current) {
            0
        } else {
            val progress by rememberInfiniteTransition(label = "mascot_bounce").animateFloat(
                initialValue = 0f,
                targetValue = frames.size.toFloat(),
                animationSpec = infiniteRepeatable(tween(BOUNCE_FRAME_MS * frames.size, easing = LinearEasing)),
                label = "mascot_bounce_frame",
            )
            // 프레임이 바뀔 때만 다시 그린다
            val index by remember { derivedStateOf { progress.toInt().coerceIn(0, frames.lastIndex) } }
            index
        }
    MascotSprite(
        bitmap = frames[frame],
        size = size,
        contentDescription = stringResource(R.string.common_loading),
        modifier = modifier.testTag("mascot_loading"),
    )
}

object RunMascotDefaults {
    /** 로딩 · 빈 상태 · 실패 */
    val Size: Dp = 120.dp

    /** 카드 안 등 작은 자리 */
    val SizeSmall: Dp = 64.dp
}

/** 보간 없이(nearest) 정수 배율로 그린다. 배율이 소수면 픽셀 크기가 들쭉날쭉해진다. */
@Composable
private fun MascotSprite(
    bitmap: ImageBitmap,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val side =
        with(density) {
            val scale = (size.toPx() / SPRITE_PX).toInt().coerceAtLeast(1)
            (SPRITE_PX * scale).toDp()
        }
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier.size(side),
        filterQuality = FilterQuality.None,
    )
}

@ComponentPreviews
@Composable
private fun RunMascotPreview() {
    RunPreviewSurface {
        RunMascotLoading()
        Row {
            RunMascotPose.entries.forEach { RunMascot(it, size = RunMascotDefaults.SizeSmall) }
        }
    }
}
