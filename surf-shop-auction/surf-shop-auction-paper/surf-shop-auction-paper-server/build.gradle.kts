plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.shop.auction.paper.PaperMain")
    authors.add("Ammo")
    generateLibraryLoader(false)
}

dependencies {
    api(project(":surf-shop-auction:surf-shop-auction-core"))
}