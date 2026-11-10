enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pacenote"
include(":app")

include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:database")
include(":core:datastore")
include(":core:location")
include(":core:tracking")
include(":core:data")
include(":core:testing")

include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:run:api")
include(":feature:run:impl")
include(":feature:record:api")
include(":feature:record:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
include(":widget")
