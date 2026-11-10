plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.home.impl"
}

dependencies {
    implementation(projects.feature.home.api)
    implementation(projects.feature.run.api)
    implementation(projects.feature.record.api)
    implementation(projects.feature.settings.api)
}
