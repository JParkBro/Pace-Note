plugins {
    alias(libs.plugins.pacenote.android.library)
    alias(libs.plugins.pacenote.koin)
}

android {
    namespace = "jpark.bro.pacenote.core.datastore"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
}
