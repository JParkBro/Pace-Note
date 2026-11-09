plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.android.compose)
}

android {
    namespace = "jpark.bro.pacenote.core.ui"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.turbine)
}
