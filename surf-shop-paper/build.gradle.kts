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
    }
}

dependencies {
    api(project(":surf-shop-core"))
    runtimeOnly(project(":surf-shop-backend"))
    compileOnly("com.github.Heliosares:AuxProtect:1.3.1") {
        isTransitive = false
    }
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:1.21.11-3.0.1")
    compileOnly("dev.slne.surf.npc:surf-npc-api:1.21.11-1.6.1-SNAPSHOT")
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
        select("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    }
}