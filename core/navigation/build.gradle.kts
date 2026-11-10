plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.android.compose)
}

android {
    namespace = "jpark.bro.pacenote.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
