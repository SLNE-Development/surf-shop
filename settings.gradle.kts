rootProject.name = "surf-shop"
include("surf-shop-api")
include("surf-shop-microservice")
include("surf-shop-paper")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.surfapi.gradle.settings") version "1.21.11+"
}
include("surf-shop-core:surf-shop-core-common")
include("surf-shop-core:surf-shop-core-paper")