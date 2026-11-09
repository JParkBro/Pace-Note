plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.koin)
}

android {
    namespace = "jpark.bro.pacenote.core.location"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // Location → RawLocation 변환 테스트 (android.location.Location)
    testImplementation(libs.bundles.robolectric)
}
