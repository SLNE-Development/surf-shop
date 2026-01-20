plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "surf-shop"

include("surf-shop-auction:surf-shop-auction-api")
include("surf-shop-auction:surf-shop-auction-core")
include("surf-shop-auction:surf-shop-auction-paper:surf-shop-auction-paper-api")
include("surf-shop-auction:surf-shop-auction-paper:surf-shop-auction-paper-server")