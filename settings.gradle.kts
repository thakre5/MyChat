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
        // Required for certain cryptographic dependencies or SQLCipher archives if pulled via maven
        maven { url = java.net.URI("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
}

rootProject.name = "SecureP2PMessenger"
include(":app")
