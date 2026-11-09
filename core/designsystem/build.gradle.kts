plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.android.compose)
}

android {
    namespace = "jpark.bro.pacenote.core.designsystem"
}

dependencies {
    // AppTheme(type, mode)의 공개 타입
    api(projects.core.model)

    // ThemeTokensSyncTest: theme-tokens.json 파싱
    testImplementation(libs.kotlinx.serialization.json)
}
