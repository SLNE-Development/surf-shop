import dev.slne.surf.surfapi.gradle.util.registerRequired
import dev.slne.surf.surfapi.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
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
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:1.21.11-3.0.1")
    compileOnly("dev.slne.surf.npc:surf-npc-api:1.21.11-1.6.1-SNAPSHOT")
    compileOnly("de.oliver:FancyHolograms:2.9.1")
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
        select("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    }
}