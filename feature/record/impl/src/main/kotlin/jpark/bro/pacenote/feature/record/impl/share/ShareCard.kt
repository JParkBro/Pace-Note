package jpark.bro.pacenote.feature.record.impl.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import jpark.bro.pacenote.core.designsystem.AppFixedColors
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RouteCanvas
import jpark.bro.pacenote.core.designsystem.component.RouteCanvasStyle
import jpark.bro.pacenote.core.domain.route.RouteProjector
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.feature.record.impl.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val PORTRAIT_RATIO = 4f / 5f
private const val SQUARE_RATIO = 1f
private const val ROUTE_PADDING = 0.12f

// 카드 크기에 비례하는 값 (1080px 너비 기준 비율). 화면 크기와 상관없이 같은 모양으로 저장된다
private const val PADDING_RATIO = 0.07f
private const val ROUTE_HEIGHT_RATIO = 0.62f
private const val BIG_TEXT_RATIO = 0.075f
private const val SMALL_TEXT_RATIO = 0.035f
private const val BRAND_TEXT_RATIO = 0.028f
private const val LINE_HEIGHT_RATIO = 1.2f

private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.M.d (E)", Locale.KOREAN)

/**
 * 공유 카드 본체 (theme-guide 5.11). 앱 테마와 독립: 배경은 route-canvas · 사진 + image-scrim · 투명,
 * 글자는 on-image만 쓰고 primary · accent를 쓰지 않는다. 지도 · 장소명 · 좌표는 넣지 않는다.
 */
@Composable
internal fun ShareCard(
    state: ShareCardState,
    photo: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val ratio = if (state.background == ShareBackground.TRANSPARENT) SQUARE_RATIO else PORTRAIT_RATIO
    BoxWithConstraints(modifier = modifier.fillMaxWidth().aspectRatio(ratio)) {
        val width = maxWidth
        CardBackground(state.background, photo)
        Column(modifier = Modifier.fillMaxSize().padding(width * PADDING_RATIO)) {
            val projected =
                remember(state.route, ratio) { RouteProjector.project(state.route, routeAspect(ratio), ROUTE_PADDING) }
            RouteCanvas(
                route = projected,
                style = RouteCanvasStyle.SHARE,
                modifier = Modifier.fillMaxWidth().weight(ROUTE_HEIGHT_RATIO),
            )
            Metrics(state, width, Modifier.weight(1f - ROUTE_HEIGHT_RATIO))
        }
    }
}

/** 카드 안쪽(여백 제외)의 경로 영역 가로 ÷ 세로 */
private fun routeAspect(cardRatio: Float): Float {
    val inner = 1f - 2 * PADDING_RATIO
    return inner / ((inner / cardRatio) * ROUTE_HEIGHT_RATIO)
}

@Composable
private fun CardBackground(
    background: ShareBackground,
    photo: ImageBitmap?,
) {
    when (background) {
        ShareBackground.DEFAULT -> {
            Box(Modifier.fillMaxSize().background(AppThemeTokens.colors.routeCanvas))
        }

        ShareBackground.PHOTO -> {
            if (photo != null) {
                Image(
                    bitmap = photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(Modifier.fillMaxSize().background(AppFixedColors.imageScrim))
            } else {
                Box(Modifier.fillMaxSize().background(AppThemeTokens.colors.routeCanvas))
            }
        }

        ShareBackground.TRANSPARENT -> {
            Box(Modifier.fillMaxSize().background(Color.Transparent))
        }
    }
}

@Composable
private fun Metrics(
    state: ShareCardState,
    cardWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val unit = state.distanceUnit
    val unitLabel = stringResource(RunFormat.unitLabelRes(unit))
    val big = cardText(cardWidth, BIG_TEXT_RATIO, AppThemeTokens.typography.metricHuge)
    val small = cardText(cardWidth, SMALL_TEXT_RATIO, AppThemeTokens.typography.bodyLg)
    val brand = cardText(cardWidth, BRAND_TEXT_RATIO, AppThemeTokens.typography.labelMd)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        if (ShareField.DISTANCE in state.fields) {
            Text(
                text = "${RunFormat.distance(state.distanceM, unit)} $unitLabel",
                style = big,
                color = AppFixedColors.onImage,
            )
        }
        val details =
            buildList {
                if (ShareField.TIME in state.fields) add(RunFormat.duration(state.movingMs))
                if (ShareField.PACE in state.fields) add(RunFormat.pace(state.averagePaceSecPerKm, unit))
                if (ShareField.DATE in state.fields) {
                    add(DATE_FORMAT.format(Instant.ofEpochMilli(state.startedAt).atZone(ZoneId.of(state.zoneId))))
                }
            }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                text = details.joinToString("  ·  "),
                style = small,
                color = AppFixedColors.onImage,
                modifier = Modifier.weight(1f),
            )
            Text(text = stringResource(R.string.record_share_brand), style = brand, color = AppFixedColors.onImage)
        }
    }
}

/** 카드 너비에 비례하는 글자 크기 (글꼴 크기 설정과 상관없이 같은 카드가 나오도록 sp가 아니라 카드 비율로) */
@Composable
private fun cardText(
    cardWidth: Dp,
    ratio: Float,
    base: TextStyle,
): TextStyle {
    val px = cardWidth.value * ratio
    val size: TextUnit = (px / LocalDensity.current.fontScale).sp
    return base.copy(fontSize = size, lineHeight = size * LINE_HEIGHT_RATIO)
}
