plugins {
    alias(libs.plugins.pacenote.android.library)
}

android {
    namespace = "jpark.bro.pacenote.core.testing"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.data)
    api(projects.core.location)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
