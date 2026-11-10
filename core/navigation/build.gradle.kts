plugins {
    alias(libs.plugins.pacenote.android.library)
}

android {
    namespace = "jpark.bro.pacenote.core.navigation"
}

// Navigator · 탭별 백스택은 첫 화면(feature:run)을 만들 때 추가한다
