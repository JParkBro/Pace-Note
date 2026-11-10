plugins {
    alias(libs.plugins.pacenote.feature.impl)
}

android {
    namespace = "jpark.bro.pacenote.feature.record.impl"
}

dependencies {
    implementation(projects.feature.record.api)
    implementation(projects.feature.run.api)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.androidx.paging.testing)
}
