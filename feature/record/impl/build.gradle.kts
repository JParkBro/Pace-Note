plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.record.impl"
}

dependencies {
    implementation(projects.feature.record.api)
    implementation(projects.feature.run.api)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.activity.compose)
    // 공유 카드: FileProvider · Bitmap.scale
    implementation(libs.androidx.core.ktx)
    implementation(projects.core.common)
    testImplementation(libs.androidx.paging.testing)
}
