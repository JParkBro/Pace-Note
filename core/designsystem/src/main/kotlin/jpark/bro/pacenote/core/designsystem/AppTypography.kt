package jpark.bro.pacenote.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Pretendard (SIL OFL 1.1, res/raw/pretendard_ofl.txt) */
val Pretendard =
    FontFamily(
        Font(R.font.pretendard_regular, FontWeight.Normal),
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
        Font(R.font.pretendard_bold, FontWeight.Bold),
        Font(R.font.pretendard_extra_bold, FontWeight.ExtraBold),
    )

/**
 * 글자 스타일 13개 (theme-guide.md 9장).
 * 수치 스타일(metric)은 tabular-nums(tnum)로 값이 바뀌어도 폭이 흔들리지 않게 한다.
 */
@Immutable
data class AppTypography(
    /** 러닝 중 거리 */
    val metricHuge: TextStyle,
    /** 홈 진행 링 안 거리 */
    val metricXl: TextStyle,
    /** 러닝 중 시간 · 페이스, 결과 · 상세의 거리 */
    val metricLg: TextStyle,
    /** 수치 타일 */
    val metricMd: TextStyle,
    /** 화면 큰 제목 */
    val headlineLg: TextStyle,
    /** 섹션 제목, 시트 제목 */
    val headlineMd: TextStyle,
    /** 카드 제목 */
    val headlineSm: TextStyle,
    /** 강조 본문 */
    val bodyLg: TextStyle,
    /** 기본 본문 */
    val bodyMd: TextStyle,
    /** 보조 설명 · 날짜 · 수치 라벨 */
    val bodySm: TextStyle,
    /** 버튼 */
    val labelLg: TextStyle,
    /** 칩 · 탭 */
    val labelMd: TextStyle,
    /** 뱃지 · 캡션 */
    val labelSm: TextStyle,
)

/** 앱 글자 스타일. Compose 테마 밖(Glance 위젯)에서는 이 값을 직접 쓴다 */
val DefaultTypography =
    AppTypography(
        metricHuge =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 72.sp,
                lineHeight = 76.sp,
                letterSpacing = (-0.03).em,
                fontFeatureSettings = "tnum",
            ),
        metricXl =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                letterSpacing = (-0.03).em,
                fontFeatureSettings = "tnum",
            ),
        metricLg =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em,
                fontFeatureSettings = "tnum",
            ),
        metricMd =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.02).em,
                fontFeatureSettings = "tnum",
            ),
        headlineLg =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.01).em,
            ),
        headlineMd =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.01).em,
            ),
        headlineSm =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.em,
            ),
        bodyLg =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em,
            ),
        bodyMd =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em,
            ),
        bodySm =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.01.em,
            ),
        labelLg =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em,
            ),
        labelMd =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.01.em,
            ),
        labelSm =
            TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.02.em,
            ),
    )

internal fun AppTypography.toMaterialTypography(): Typography =
    Typography(
        displayLarge = metricHuge,
        displayMedium = metricXl,
        headlineLarge = headlineLg,
        headlineMedium = headlineMd,
        headlineSmall = headlineSm,
        titleLarge = headlineSm,
        titleMedium = labelLg,
        titleSmall = labelMd,
        bodyLarge = bodyLg,
        bodyMedium = bodyMd,
        bodySmall = bodySm,
        labelLarge = labelLg,
        labelMedium = labelMd,
        labelSmall = labelSm,
    )
