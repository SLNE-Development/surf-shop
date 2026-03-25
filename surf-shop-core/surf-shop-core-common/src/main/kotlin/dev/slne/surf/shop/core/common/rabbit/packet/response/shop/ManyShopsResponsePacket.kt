package dev.slne.surf.shop.core.common.rabbit.packet.response.shop

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.shop.api.shop.Shop
import kotlinx.serialization.Serializable

@Serializable
data class ManyShopsResponsePacket(
    val shops: List<Shop>
) : RabbitResponsePacket()
