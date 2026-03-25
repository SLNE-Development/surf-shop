package dev.slne.surf.shop.core.common.rabbit.packet.request.deal

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.response.deal.SingleDealResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class BuyRequestPacket(
    val shop: Shop,
    val amount: Int,
    val buyer: SerializableUUID,
    val boughtAt: SerializableOffsetDateTime
) : RabbitRequestPacket<SingleDealResponsePacket>()
