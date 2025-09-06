package dev.slne.surf.shop.server

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.cloud.api.server.plugin.bootstrap.BootstrapContext
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.shop.ShopApplication
import dev.slne.surf.shop.core.common.ContextHolderImpl

class PluginBootstrap : StandalonePluginBootstrap {
    override suspend fun bootstrap(context: BootstrapContext) {
        ContextHolderImpl.INSTANCE.context =
            CloudInstance.startSpringApplication(ShopApplication::class)
    }
}