package jpark.bro.pacenote.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("AppTheme 밖에서 AppThemeTokens를 사용했습니다.") }
val LocalAppTypography = staticCompositionLocalOf<AppTypography> { error("AppTheme 밖에서 AppThemeTokens를 사용했습니다.") }

val LocalProgressBrush = staticCompositionLocalOf<Brush> { error("AppTheme 밖에서 AppThemeTokens를 사용했습니다.") }

/**
 * 앱 테마. 선택한 테마 · 모드는 DataStore에 저장하고 위젯도 같은 팔레트를 쓴다 (theme-guide.md).
 * Material 컴포넌트도 토큰 색 · 글자 스타일을 쓰도록 colorScheme · typography를 매핑한다.
 */
@Composable
fun AppTheme(
    type: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(mode)
    val colors = palette(type, dark)
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides DefaultTypography,
        LocalProgressBrush provides progressBrush(type, dark, colors),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(dark),
            typography = DefaultTypography.toMaterialTypography(),
            content = content,
        )
    }
}

/** 화면 모드를 실제 다크 여부로 바꾼다. SYSTEM은 기기 설정을 따른다. */
@Composable
fun isDarkTheme(mode: AppThemeMode): Boolean =
    when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

/** 진행바 · 진행 링 채움. `volt` 라이트는 primary가 거의 검정이라 accent 단색이다 (theme-guide.md 5.6). */
private fun progressBrush(
    type: AppThemeType,
    dark: Boolean,
    colors: AppColors,
): Brush =
    if (type == AppThemeType.VOLT && !dark) {
        SolidColor(colors.accent)
    } else {
        Brush.horizontalGradient(listOf(colors.primary, colors.accent))
    }

object AppThemeTokens {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val progressBrush: Brush
        @Composable
        @ReadOnlyComposable
        get() = LocalProgressBrush.current
}

private fun AppColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primarySoft,
        onPrimaryContainer = onPrimarySoft,
        secondary = accent,
        secondaryContainer = accentSoft,
        onSecondaryContainer = onAccentSoft,
        background = bg,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = fill,
        onSurfaceVariant = textSecondary,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceRaised,
        surfaceContainerHighest = surfaceRaised,
        outline = borderStrong,
        outlineVariant = border,
        error = danger,
        errorContainer = dangerSoft,
        onErrorContainer = danger,
        scrim = overlay,
    )
}
