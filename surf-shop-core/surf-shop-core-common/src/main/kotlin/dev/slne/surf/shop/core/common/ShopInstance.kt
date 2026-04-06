package dev.slne.surf.shop.core.common

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.rabbitmq.api.RabbitMQApi

private val instance = requiredService<ShopInstance>()

interface ShopInstance {
    val rabbitApi: RabbitMQApi

    companion object : ShopInstance by instance {
        val INSTANCE get() = instance
    }
}