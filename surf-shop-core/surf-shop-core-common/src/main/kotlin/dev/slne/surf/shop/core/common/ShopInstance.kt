package dev.slne.surf.shop.core.common

import dev.slne.surf.rabbitmq.api.RabbitMQApi
import dev.slne.surf.surfapi.core.api.util.requiredService


private val instance = requiredService<ShopInstance>()

interface ShopInstance {
    val rabbitApi: RabbitMQApi

    companion object : ShopInstance by instance {
        val INSTANCE get() = instance
    }
}