package dev.slne.surf.shop.paper

import com.google.auto.service.AutoService
import dev.slne.surf.shop.core.common.ShopInstance
import dev.slne.surf.shop.core.paper.PaperLoader
import dev.slne.surf.shop.core.paper.PaperShopInstance
import net.kyori.adventure.util.Services

@AutoService(ShopInstance::class)
class PaperShopInstanceImpl : PaperShopInstance, Services.Fallback {
    override val paperLoader = PaperLoader(plugin.dataPath)
}