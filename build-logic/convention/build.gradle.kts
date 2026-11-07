import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "jpark.bro.pacenote.buildlogic"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.pacenote.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.pacenote.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.pacenote.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.pacenote.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("koin") {
            id = libs.plugins.pacenote.koin.get().pluginId
            implementationClass = "KoinConventionPlugin"
        }
        register("room") {
            id = libs.plugins.pacenote.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("featureApi") {
            id = libs.plugins.pacenote.feature.api.get().pluginId
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = libs.plugins.pacenote.feature.impl.get().pluginId
            implementationClass = "FeatureImplConventionPlugin"
        }
    }
}
