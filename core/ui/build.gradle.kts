plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.android.compose)
}

android {
    namespace = "jpark.bro.pacenote.core.ui"
}

dependencies {
    api(projects.core.model)
    // 기능 간 공유 UI 조각(경로 카드 · 요약 수치 · 구간 표)
    api(projects.core.domain)
    implementation(projects.core.designsystem)
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.turbine)
}
