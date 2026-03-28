package dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shopchest.ManyStaticShopChestsResponsePacket
import kotlinx.serialization.Serializable

@Serializable
object LoadStaticShopChestsRequestPacket : RabbitRequestPacket<ManyStaticShopChestsResponsePacket>()
