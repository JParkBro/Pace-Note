plugins {
    alias(libs.plugins.pacenote.jvm.library)
    `java-test-fixtures`
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)

    testFixturesImplementation(projects.core.model)
    testFixturesImplementation(libs.kotlinx.serialization.json)
}
