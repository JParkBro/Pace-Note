package jpark.bro.pacenote.core.model

/** 컬러 테마 4종 (theme-guide.md 3장) */
enum class AppThemeType {
    /** 볼트 라임 (기본) */
    VOLT,

    /** 선라이즈 */
    SUNRISE,

    /** 리버 블루 */
    RIVER,

    /** 잉크 모노 */
    INK,
}

/** 화면 모드. SYSTEM은 기기 다크 모드 설정을 따른다. */
enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}
