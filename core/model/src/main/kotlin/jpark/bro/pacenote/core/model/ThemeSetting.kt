package jpark.bro.pacenote.core.model

/** 사용자가 고른 테마 · 모드. 기본은 볼트 라임 + 시스템 모드 (theme-guide.md 3장). */
data class ThemeSetting(
    val type: AppThemeType = AppThemeType.VOLT,
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
)
