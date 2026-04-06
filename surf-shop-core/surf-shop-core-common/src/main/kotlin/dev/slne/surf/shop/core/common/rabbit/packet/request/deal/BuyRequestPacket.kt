package dev.slne.surf.shop.core.common.rabbit.packet.request.deal

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.response.deal.SingleDealResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class BuyRequestPacket(
    val shop: Shop,
    val amount: Int,
    val buyer: SerializableUUID,
    val boughtAt: SerializableOffsetDateTime
) : RabbitRequestPacket<SingleDealResponsePacket>()
