plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
    implementation(libs.androidx.activity.compose)
}
