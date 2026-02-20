pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "PhantomNet"
include(":app")
include(":core:crypto")
include(":core:network")
include(":core:database")
include(":core:identity")
include(":feature:onboarding")
include(":feature:settings")
include(":feature:sync")
