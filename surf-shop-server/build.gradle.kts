import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("dev.slne.java-conventions")
    id("dev.slne.java-shadow-conventions")
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.2.2"
}

dependencies {
    api(project(":surf-shop-api"))

    compileOnlyApi(libs.dev.slne.surf.data.api)
    compileOnlyApi(libs.dev.jorel.commandapi.bukkit.core)
    compileOnlyApi(libs.dev.slne.surf.gui.api)

    compileOnly(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
}

tasks {
    runServer {
        minecraftVersion("1.20.4")

        downloadPlugins {
            modrinth("commandapi", "9.3.0")
        }
    }
}

description = "surf-shop-server"

paper {
    name = "SurfShopBukkit"
    version = project.version.toString()
    apiVersion = "1.20"
    description = "Shop plugin for surf"
    authors = listOf("twisti", "ammo", "SLNE Development")

    main = "dev.slne.surf.shop.server.BukkitMain"
    loader = "dev.slne.surf.shop.server.BukkitLoader"
    bootstrapper = "dev.slne.surf.shop.server.BukkitBootstrap"

    generateLibrariesJson = true

    serverDependencies {
        register("surf-data-bukkit")
        register("CommandAPI")
        register("SurfGuiBukkit")
        register("SurfTransactionBukkit") // FIXME: 25.01.2024 21:03 - correct name?
    }

    permissions {
        register("surf.shop.*") {
            default = BukkitPluginDescription.Permission.Default.OP

            childrenMap = mapOf(
                "surf.shop.item.menu.back-item" to true,
                "surf.shop.item.menu.close-item" to true,

                "surf.shop.item.main-menu.sell" to true,
                "surf.shop.item.main-menu.owner" to true,
                "surf.shop.item.main-menu.edit" to true,
                "surf.shop.item.main-menu.info" to true,
                "surf.shop.item.main-menu.shop-item" to true,
                "surf.shop.item.main-menu.buy" to true,

                "surf.shop.item.sell-menu.owner" to true,
                "surf.shop.item.sell-menu.info" to true,
                "surf.shop.item.sell-menu.buy" to true,
                "surf.shop.item.sell-menu.reset" to true,
                "surf.shop.item.sell-menu.shop-item" to true,
                "surf.shop.item.sell-menu.decrease-1000" to true,
                "surf.shop.item.sell-menu.decrease-100" to true,
                "surf.shop.item.sell-menu.decrease-10" to true,
                "surf.shop.item.sell-menu.decrease-1" to true,
                "surf.shop.item.sell-menu.increase-1" to true,
                "surf.shop.item.sell-menu.increase-10" to true,
                "surf.shop.item.sell-menu.increase-100" to true,
                "surf.shop.item.sell-menu.increase-1000" to true,

                "surf.shop.give" to true
            )
        }
    }
}

fun NamedDomainObjectContainerScope<PaperPluginDescription.DependencyDefinition>.register(
    name: String,
    required: Boolean = true,
    load: PaperPluginDescription.RelativeLoadOrder = PaperPluginDescription.RelativeLoadOrder.BEFORE,
    joinClasspath: Boolean = true
) = register(name) {
    this.required = required
    this.load = load
    this.joinClasspath = joinClasspath
}
