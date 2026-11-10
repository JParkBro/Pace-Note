plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(libs.androidx.activity.compose)
    // 오픈소스 라이선스 목록 화면. 목록은 app의 oss-licenses 플러그인이 빌드 때 만든다
    implementation(libs.play.services.oss.licenses)
}
