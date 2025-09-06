package dev.slne.surf.shop.paper

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.shop.ShopApplication
import dev.slne.surf.shop.core.common.ContextHolderImpl
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap

@Suppress("UnstableApiUsage")
class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        ContextHolderImpl.INSTANCE.context =
            CloudInstance.startSpringApplication(ShopApplication::class)
    }
}