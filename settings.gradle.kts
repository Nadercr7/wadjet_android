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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Wadjet"

include(":app")

// Core modules
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:firebase")
include(":core:ml")
include(":core:common")
include(":core:ui")

// Feature modules
include(":feature:auth")
include(":feature:landing")
include(":feature:scan")
include(":feature:dictionary")
include(":feature:explore")
include(":feature:chat")
include(":feature:stories")
include(":feature:dashboard")
include(":feature:settings")
include(":feature:feedback")
 