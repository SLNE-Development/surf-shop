package dev.slne.surf.shop.core.common.rabbit.packet.response.shopchest

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import kotlinx.serialization.Serializable

@Serializable
data class ManyStaticShopChestsResponsePacket(
    val chests: List<StaticShopChest>
) : RabbitResponsePacket()
