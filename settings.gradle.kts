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
        maven("https://jitpack.io")
        maven("https//io.github.nefilim.kjwt:kjwt-core:0.3.0")
//        maven("https://repo.sendbird.com/public/maven")
    }
}


rootProject.name = "Shots"
include(":app")
