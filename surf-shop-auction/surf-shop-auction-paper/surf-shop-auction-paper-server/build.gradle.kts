plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.shop.auction.paper.server.PaperMain")
    authors.add("Ammo")
    generateLibraryLoader(false)

    withCorePaper()
}

dependencies {
    api(project(":surf-shop-auction:surf-shop-auction-paper:surf-shop-auction-paper-api"))
    api(project(":surf-shop-auction:surf-shop-auction-core"))
}