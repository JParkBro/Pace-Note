// 생성 파일 — 직접 고치지 않는다. 원본: docs/run/assets/theme-tokens.json
// 다시 만들기: python3 scripts/generate-theme-colors.py
@file:Suppress("MagicNumber")

package jpark.bro.pacenote.core.designsystem

import androidx.compose.ui.graphics.Color
import jpark.bro.pacenote.core.model.AppThemeType

/** 볼트 라임 · 라이트 */
internal val VoltLight =
    AppColors(
        primary = Color(0xFF1E2810),
        primaryPressed = Color(0xFF323F1C),
        onPrimary = Color(0xFFD6FF3D),
        primarySoft = Color(0xFFEEF8CF),
        onPrimarySoft = Color(0xFF3B5400),
        accent = Color(0xFF9AD615),
        accentSoft = Color(0xFFF2F9DE),
        onAccentSoft = Color(0xFF4A6608),
        bg = Color(0xFFF7F8F3),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        fill = Color(0xFFEEF0E8),
        overlay = Color(0x730E1208),
        border = Color(0xFFE4E7DC),
        borderStrong = Color(0xFFCBD0BE),
        textPrimary = Color(0xFF171C10),
        textSecondary = Color(0xFF5C6352),
        textTertiary = Color(0xFF8A9080),
        danger = Color(0xFFC62828),
        dangerSoft = Color(0xFFFDECEC),
        success = Color(0xFF127235),
        successSoft = Color(0xFFE7F5EC),
        warning = Color(0xFFB45309),
        warningSoft = Color(0xFFFDF3E1),
        calSunday = Color(0xFFC62828),
        calSaturday = Color(0xFF2563EB),
        shadow = Color(0x1F1E2810),
        routeCanvas = Color(0xFF1E2810),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFD6FF3D),
    )

/** 볼트 라임 · 다크 */
internal val VoltDark =
    AppColors(
        primary = Color(0xFFC8F53A),
        primaryPressed = Color(0xFFD9FA74),
        onPrimary = Color(0xFF141A05),
        primarySoft = Color(0xFF252F10),
        onPrimarySoft = Color(0xFFDDF98A),
        accent = Color(0xFF7DD3A0),
        accentSoft = Color(0xFF15291E),
        onAccentSoft = Color(0xFFB5EBCB),
        bg = Color(0xFF0D0F09),
        surface = Color(0xFF161911),
        surfaceRaised = Color(0xFF20241A),
        fill = Color(0xFF23281C),
        overlay = Color(0x99000000),
        border = Color(0xFF262B1E),
        borderStrong = Color(0xFF3A4130),
        textPrimary = Color(0xFFF1F4EA),
        textSecondary = Color(0xFFA9B09D),
        textTertiary = Color(0xFF7B8270),
        danger = Color(0xFFF87171),
        dangerSoft = Color(0xFF3A1C1F),
        success = Color(0xFF5BD69A),
        successSoft = Color(0xFF163024),
        warning = Color(0xFFFBBF55),
        warningSoft = Color(0xFF3A2C12),
        calSunday = Color(0xFFF87171),
        calSaturday = Color(0xFF60A5FA),
        shadow = Color(0x73000000),
        routeCanvas = Color(0xFF1A2010),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFC8F53A),
    )

/** 선라이즈 · 라이트 */
internal val SunriseLight =
    AppColors(
        primary = Color(0xFFC2410C),
        primaryPressed = Color(0xFFA3360A),
        onPrimary = Color(0xFFFFFFFF),
        primarySoft = Color(0xFFFFEEE3),
        onPrimarySoft = Color(0xFF9A3412),
        accent = Color(0xFFF59E0B),
        accentSoft = Color(0xFFFEF4DC),
        onAccentSoft = Color(0xFF8A5300),
        bg = Color(0xFFFBF8F5),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        fill = Color(0xFFF4EFEA),
        overlay = Color(0x731E1008),
        border = Color(0xFFEEE6DF),
        borderStrong = Color(0xFFD9CCC1),
        textPrimary = Color(0xFF221911),
        textSecondary = Color(0xFF6B5F55),
        textTertiary = Color(0xFF958A80),
        danger = Color(0xFFC62828),
        dangerSoft = Color(0xFFFDECEC),
        success = Color(0xFF127235),
        successSoft = Color(0xFFE7F5EC),
        warning = Color(0xFFB45309),
        warningSoft = Color(0xFFFDF3E1),
        calSunday = Color(0xFFC62828),
        calSaturday = Color(0xFF2563EB),
        shadow = Color(0x1FC2410C),
        routeCanvas = Color(0xFF2E1609),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFFDBA4A),
    )

/** 선라이즈 · 다크 */
internal val SunriseDark =
    AppColors(
        primary = Color(0xFFFB8A4C),
        primaryPressed = Color(0xFFFCA572),
        onPrimary = Color(0xFF2A1003),
        primarySoft = Color(0xFF3A1F12),
        onPrimarySoft = Color(0xFFFFC9A8),
        accent = Color(0xFFFBBF55),
        accentSoft = Color(0xFF3A2C12),
        onAccentSoft = Color(0xFFFDE0A6),
        bg = Color(0xFF14100D),
        surface = Color(0xFF1D1814),
        surfaceRaised = Color(0xFF28211C),
        fill = Color(0xFF2E2620),
        overlay = Color(0x99000000),
        border = Color(0xFF2F2721),
        borderStrong = Color(0xFF463A31),
        textPrimary = Color(0xFFF8F1EB),
        textSecondary = Color(0xFFB8AA9E),
        textTertiary = Color(0xFF857A70),
        danger = Color(0xFFF87171),
        dangerSoft = Color(0xFF3A1C1F),
        success = Color(0xFF5BD69A),
        successSoft = Color(0xFF163024),
        warning = Color(0xFFFBBF55),
        warningSoft = Color(0xFF3A2C12),
        calSunday = Color(0xFFF87171),
        calSaturday = Color(0xFF60A5FA),
        shadow = Color(0x73000000),
        routeCanvas = Color(0xFF2A160C),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFFBBF55),
    )

/** 리버 블루 · 라이트 */
internal val RiverLight =
    AppColors(
        primary = Color(0xFF0B6E86),
        primaryPressed = Color(0xFF095A6E),
        onPrimary = Color(0xFFFFFFFF),
        primarySoft = Color(0xFFE2F4F7),
        onPrimarySoft = Color(0xFF0A5C70),
        accent = Color(0xFF2BC4D8),
        accentSoft = Color(0xFFE3F7FA),
        onAccentSoft = Color(0xFF0B6373),
        bg = Color(0xFFF5F9FA),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        fill = Color(0xFFEBF2F4),
        overlay = Color(0x7306161C),
        border = Color(0xFFDEE8EB),
        borderStrong = Color(0xFFC3D3D8),
        textPrimary = Color(0xFF0F1E23),
        textSecondary = Color(0xFF53656B),
        textTertiary = Color(0xFF839297),
        danger = Color(0xFFC62828),
        dangerSoft = Color(0xFFFDECEC),
        success = Color(0xFF127235),
        successSoft = Color(0xFFE7F5EC),
        warning = Color(0xFFB45309),
        warningSoft = Color(0xFFFDF3E1),
        calSunday = Color(0xFFC62828),
        calSaturday = Color(0xFF2563EB),
        shadow = Color(0x1F0B6E86),
        routeCanvas = Color(0xFF0B2D37),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFF5FE0EE),
    )

/** 리버 블루 · 다크 */
internal val RiverDark =
    AppColors(
        primary = Color(0xFF45C9DE),
        primaryPressed = Color(0xFF76D9E8),
        onPrimary = Color(0xFF032029),
        primarySoft = Color(0xFF0F2F38),
        onPrimarySoft = Color(0xFFA6E8F2),
        accent = Color(0xFF7DD3FC),
        accentSoft = Color(0xFF0C2A3D),
        onAccentSoft = Color(0xFFBAE6FD),
        bg = Color(0xFF091316),
        surface = Color(0xFF0F1C20),
        surfaceRaised = Color(0xFF16272C),
        fill = Color(0xFF1A2C31),
        overlay = Color(0x99000000),
        border = Color(0xFF1C3036),
        borderStrong = Color(0xFF2B454D),
        textPrimary = Color(0xFFEDF6F8),
        textSecondary = Color(0xFF98ADB3),
        textTertiary = Color(0xFF6C8188),
        danger = Color(0xFFF87171),
        dangerSoft = Color(0xFF3A1C1F),
        success = Color(0xFF5BD69A),
        successSoft = Color(0xFF163024),
        warning = Color(0xFFFBBF55),
        warningSoft = Color(0xFF3A2C12),
        calSunday = Color(0xFFF87171),
        calSaturday = Color(0xFF60A5FA),
        shadow = Color(0x73000000),
        routeCanvas = Color(0xFF0B2830),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFF45C9DE),
    )

/** 잉크 모노 · 라이트 */
internal val InkLight =
    AppColors(
        primary = Color(0xFF18181B),
        primaryPressed = Color(0xFF3F3F46),
        onPrimary = Color(0xFFFFFFFF),
        primarySoft = Color(0xFFEFEFF1),
        onPrimarySoft = Color(0xFF27272A),
        accent = Color(0xFF8E8E96),
        accentSoft = Color(0xFFF2F2F4),
        onAccentSoft = Color(0xFF3F3F46),
        bg = Color(0xFFF7F7F8),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFFFFFFF),
        fill = Color(0xFFEEEEF0),
        overlay = Color(0x730A0A0C),
        border = Color(0xFFE4E4E7),
        borderStrong = Color(0xFFCDCDD3),
        textPrimary = Color(0xFF18181B),
        textSecondary = Color(0xFF5E5E66),
        textTertiary = Color(0xFF8E8E96),
        danger = Color(0xFFC62828),
        dangerSoft = Color(0xFFFDECEC),
        success = Color(0xFF127235),
        successSoft = Color(0xFFE7F5EC),
        warning = Color(0xFFB45309),
        warningSoft = Color(0xFFFDF3E1),
        calSunday = Color(0xFFC62828),
        calSaturday = Color(0xFF2563EB),
        shadow = Color(0x1A000000),
        routeCanvas = Color(0xFF18181B),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFFFFFFF),
    )

/** 잉크 모노 · 다크 */
internal val InkDark =
    AppColors(
        primary = Color(0xFFF4F4F5),
        primaryPressed = Color(0xFFD4D4D8),
        onPrimary = Color(0xFF111113),
        primarySoft = Color(0xFF27272A),
        onPrimarySoft = Color(0xFFE4E4E7),
        accent = Color(0xFFA1A1AA),
        accentSoft = Color(0xFF232326),
        onAccentSoft = Color(0xFFD4D4D8),
        bg = Color(0xFF0C0C0D),
        surface = Color(0xFF161618),
        surfaceRaised = Color(0xFF202023),
        fill = Color(0xFF252528),
        overlay = Color(0x99000000),
        border = Color(0xFF26262A),
        borderStrong = Color(0xFF3A3A40),
        textPrimary = Color(0xFFF4F4F5),
        textSecondary = Color(0xFFA8A8B0),
        textTertiary = Color(0xFF76767E),
        danger = Color(0xFFF87171),
        dangerSoft = Color(0xFF3A1C1F),
        success = Color(0xFF5BD69A),
        successSoft = Color(0xFF163024),
        warning = Color(0xFFFBBF55),
        warningSoft = Color(0xFF3A2C12),
        calSunday = Color(0xFFF87171),
        calSaturday = Color(0xFF60A5FA),
        shadow = Color(0x73000000),
        routeCanvas = Color(0xFF1C1C1F),
        routeLine = Color(0xFFFFFFFF),
        routeLineMuted = Color(0x59FFFFFF),
        routeStart = Color(0xFFFFFFFF),
    )

internal fun palette(
    type: AppThemeType,
    dark: Boolean,
): AppColors =
    when (type) {
        AppThemeType.VOLT -> if (dark) VoltDark else VoltLight
        AppThemeType.SUNRISE -> if (dark) SunriseDark else SunriseLight
        AppThemeType.RIVER -> if (dark) RiverDark else RiverLight
        AppThemeType.INK -> if (dark) InkDark else InkLight
    }

/** 테마와 무관한 고정 색 (theme-guide 4장) */
object AppFixedColors {
    val onImage = Color(0xFFFFFFFF)
    val imageScrim = Color(0x59000000)
}
