rootProject.name = "surf-shop"

include("surf-shop-api")
include("surf-shop-core:surf-shop-core-common")
include("surf-shop-core:surf-shop-core-paper")
include("surf-shop-paper")
include("surf-shop-microservice")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}