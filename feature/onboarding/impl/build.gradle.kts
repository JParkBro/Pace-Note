plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.onboarding.impl"
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(libs.androidx.activity.compose)
}
