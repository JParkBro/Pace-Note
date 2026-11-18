plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.settings.impl"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        // 다른 앱 둘러보기 페이지의 from. dev flavor의 applicationIdSuffix(.dev)가 붙지 않은 출시 패키지 이름
        buildConfigField("String", "RELEASE_APPLICATION_ID", "\"${libs.versions.projectApplicationId.get()}\"")
    }
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(libs.androidx.activity.compose)
    // 오픈소스 라이선스 목록 화면. 목록은 app의 oss-licenses 플러그인이 빌드 때 만든다
    implementation(libs.play.services.oss.licenses)
}
