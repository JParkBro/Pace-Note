plugins {
    alias(libs.plugins.pacenote.android.application)
    alias(libs.plugins.pacenote.android.compose)
}

android {
    namespace = "jpark.bro.pacenote"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
