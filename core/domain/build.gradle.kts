plugins {
    alias(libs.plugins.pacenote.jvm.library)
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
}
