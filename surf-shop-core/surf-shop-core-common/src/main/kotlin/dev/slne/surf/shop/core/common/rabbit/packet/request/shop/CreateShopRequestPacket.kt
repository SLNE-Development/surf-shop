package dev.slne.surf.shop.core.common.rabbit.packet.request.shop

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shop.SingleShopResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class CreateShopRequestPacket(
    val itemString: String,
    val storedItemCount: Int,
    val pricePerItem: Int,
    val seller: SerializableUUID,
    val createdAt: SerializableOffsetDateTime
) : RabbitRequestPacket<SingleShopResponsePacket>()
