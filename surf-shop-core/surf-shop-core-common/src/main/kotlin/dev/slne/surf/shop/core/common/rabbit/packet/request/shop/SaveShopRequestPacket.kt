package dev.slne.surf.shop.core.common.rabbit.packet.request.shop

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.shop.api.shop.Shop
import kotlinx.serialization.Serializable

@Serializable
data class SaveShopRequestPacket(
    val shop: Shop
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
