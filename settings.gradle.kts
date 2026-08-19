pluginManagement {
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
rootProject.name = "XCan"
include(":app")
include(":core:model")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:bluetooth")
include(":core:ui")
include(":feature:dashboard")
include(":feature:maintenance")
include(":feature:config")
include(":feature:diagnostics")
