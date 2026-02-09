plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.shop.paper.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")
}

dependencies {
    api(project(":surf-shop-core"))
    runtimeOnly(project(":surf-shop-backend"))
}