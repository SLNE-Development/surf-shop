package dev.slne.surf.shop.core.common.rabbit.packet.request.shop

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shop.SingleShopResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class CreateShopRequestPacket(
    val itemString: String,
    val storedItemCount: Int,
    val pricePerItem: Double,
    val seller: SerializableUUID,
    val createdAt: SerializableOffsetDateTime
) : RabbitRequestPacket<SingleShopResponsePacket>()
