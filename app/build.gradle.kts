plugins {
    alias(libs.plugins.pacenote.android.application)
    alias(libs.plugins.pacenote.android.compose)
    alias(libs.plugins.pacenote.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "jpark.bro.pacenote"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.navigation)
    implementation(projects.core.tracking)
    implementation(projects.feature.home.api)
    implementation(projects.feature.run.api)
    implementation(projects.feature.run.impl)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.koin.compose)
    implementation(libs.kotlinx.serialization.json)

    // debug 빌드 전용 추적 테스트 화면 (tracking.md 8-3): 실제 GPS ↔ GPX 재생 전환, 거리 · 시간 표기
    debugImplementation(projects.core.common)
    debugImplementation(projects.core.location)
    debugImplementation(projects.core.data)
    debugImplementation(projects.core.ui)

    // KoinModulesTest
    testImplementation(libs.bundles.robolectric)
    testImplementation(projects.core.testing)
}
