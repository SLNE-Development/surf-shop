import dev.slne.surf.api.gradle.util.registerRequired
import dev.slne.surf.api.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.shop.paper.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")

    serverDependencies {
        registerSoft("surf-npc-paper")
        registerSoft("AuxProtect")
        registerRequired("surf-transaction-paper")
        registerRequired("surf-rabbitmq-paper")
        registerSoft("FancyHolograms")
    }
}

dependencies {
    api(projects.surfShopCore.surfShopCorePaper)

    compileOnly(files("libs/auxprotect-paper-1.3.4-pre6-all.jar"))
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:+")
    compileOnly("dev.slne.surf.npc:surf-npc-api:+")
    compileOnly("de.oliver:FancyHolograms:2.9.1")
}