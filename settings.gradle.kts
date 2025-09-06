plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "surf-shop"

include("surf-shop-api:common-api")
include("surf-shop-core:common-core")
include("surf-shop-paper")
include("surf-shop-server")
