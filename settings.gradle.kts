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

rootProject.name = "MyCodeCalender"
include(":app")
include(":core:common", ":core:designsystem", ":core:model", ":core:network", ":core:database", ":core:datastore", ":core:notifications", ":core:calendar", ":core:analytics", ":core:navigation")
include(":data:local", ":data:remote", ":data:repository", ":data:mapper")
include(":domain:model", ":domain:repository", ":domain:usecase")
include(":feature:onboarding", ":feature:home", ":feature:contests", ":feature:contestdetail", ":feature:platforms", ":feature:platformdetail", ":feature:resources", ":feature:settings")
include(":widget", ":sync")
include(":backend")
