package dev.slne.surf.shop.core.paper

import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.shop.core.common.ShopInstance

interface PaperShopInstance : ShopInstance {
    val paperLoader: PaperLoader

    override val rabbitApi: ClientRabbitMQApi get() = paperLoader.rabbitApi

    companion object : PaperShopInstance by ShopInstance.INSTANCE as PaperShopInstance {
        val INSTANCE get() = ShopInstance.INSTANCE
    }
}