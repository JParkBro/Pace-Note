plugins {
    alias(libs.plugins.pacenote.android.library)
}

android {
    namespace = "jpark.bro.pacenote.core.ui"
}

// MviViewModel · ObserveEvents · 권한 헬퍼는 첫 화면(feature:run)을 만들 때 compose 플러그인과 함께 추가한다
dependencies {
    api(projects.core.model)
}
