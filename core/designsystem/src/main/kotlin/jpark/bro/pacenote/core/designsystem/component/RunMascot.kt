package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 로딩 애니메이션 프레임 수 · 한 프레임 시간 (숨쉬기 시트: 8프레임 · 8fps, 한 바퀴 1초) */
private const val LOADING_FRAMES = 8
private const val LOADING_FRAME_MILLIS = 125

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
 * 정지 마스코트. [size] 안에 들어가는 가장 큰 **정수 배율**로, 보간 없이 그려 픽셀이 번지지 않는다.
 * 옆에 같은 뜻의 문구가 있으면 [contentDescription]은 null(장식, 스크린 리더가 건너뜀).
 */
@Composable
fun RunMascot(
    pose: RunMascotPose,
    modifier: Modifier = Modifier,
    size: Dp = RunMascotDefaults.Size,
    contentDescription: String? = null,
) {
    MascotSprite(
        sheet = ImageBitmap.imageResource(pose.res),
        frameCount = 1,
        frame = 0,
        modifier = modifier.size(size).mascotSemantics(contentDescription, loading = false),
    )
}

/**
 * 커스텀 로딩: 마스코트가 숨쉬며 눈을 깜빡인다 (8프레임 × 125ms 반복). `CircularProgressIndicator` 대신 쓴다.
 * 스크린 리더에는 "불러오는 중" + 진행 중(불확정)으로 읽힌다.
 */
@Composable
fun RunMascotLoading(
    modifier: Modifier = Modifier,
    size: Dp = RunMascotDefaults.Size,
) {
    MascotSprite(
        sheet = ImageBitmap.imageResource(R.drawable.mascot_breath_sheet_80x80_8f),
        frameCount = LOADING_FRAMES,
        frame = rememberLoopFrame(LOADING_FRAMES, LOADING_FRAME_MILLIS),
        modifier =
            modifier
                .size(size)
                .testTag("mascot_loading")
                .mascotSemantics(stringResource(R.string.common_loading), loading = true),
    )
}

object RunMascotDefaults {
    /** 로딩 · 빈 상태 · 실패 */
    val Size: Dp = 120.dp

    /** 카드 안 등 작은 자리 */
    val SizeSmall: Dp = 64.dp
}

/**
 * 0 → [frameCount] - 1을 [frameMillis]마다 넘기며 끝없이 반복.
 * Preview · 스크린샷(`LocalInspectionMode`)에서는 결과가 매번 같도록 첫 프레임으로 고정한다.
 */
@Composable
private fun rememberLoopFrame(
    frameCount: Int,
    frameMillis: Int,
): Int {
    if (LocalInspectionMode.current) return 0
    val frame by rememberInfiniteTransition(label = "mascot").animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(frameCount * frameMillis, easing = LinearEasing)),
        label = "frame",
    )
    return frame % frameCount
}

/** [availablePx] 안에 [spritePx] 그림이 들어가는 가장 큰 정수 배율. 작아도 1배는 그린다 */
internal fun mascotPixelScale(
    availablePx: Int,
    spritePx: Int,
): Int = (availablePx / spritePx).coerceAtLeast(1)

private fun Modifier.mascotSemantics(
    description: String?,
    loading: Boolean,
): Modifier =
    clearAndSetSemantics {
        if (description != null) contentDescription = description
        if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
    }

/** 가로로 프레임이 붙은 [sheet]에서 [frame]번째를 가운데에 그린다 */
@Composable
private fun MascotSprite(
    sheet: ImageBitmap,
    frameCount: Int,
    frame: Int,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val frameWidth = sheet.width / frameCount
                val scale = mascotPixelScale(minOf(size.width, size.height).toInt(), maxOf(frameWidth, sheet.height))
                val dst = IntSize(frameWidth * scale, sheet.height * scale)
                drawImage(
                    image = sheet,
                    srcOffset = IntOffset(frame * frameWidth, 0),
                    srcSize = IntSize(frameWidth, sheet.height),
                    dstOffset =
                        IntOffset(
                            ((size.width - dst.width) / 2).toInt(),
                            ((size.height - dst.height) / 2).toInt(),
                        ),
                    dstSize = dst,
                    filterQuality = FilterQuality.None,
                )
            },
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
