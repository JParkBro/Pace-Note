plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.koin)
}

android {
    namespace = "jpark.bro.pacenote.core.tracking"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.location)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(projects.core.navigation)
    // 알림 작은 아이콘
    implementation(projects.core.designsystem)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.service)

    // TrackingEngine 테스트: Fake 위치 + in-memory Room (tracking.md 8-2)
    testImplementation(projects.core.testing)
    testImplementation(libs.bundles.robolectric)
    testImplementation(libs.room.runtime)
}
