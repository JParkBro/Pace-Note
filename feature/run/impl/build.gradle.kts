plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.run.impl"
}

dependencies {
    implementation(projects.feature.run.api)
    implementation(projects.feature.record.api)
    implementation(projects.core.tracking)
    implementation(projects.core.common)
    // 기기 위치 켜기 요청 (시스템 설정 대화상자, product-spec 5-2)
    implementation(libs.play.services.location)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
