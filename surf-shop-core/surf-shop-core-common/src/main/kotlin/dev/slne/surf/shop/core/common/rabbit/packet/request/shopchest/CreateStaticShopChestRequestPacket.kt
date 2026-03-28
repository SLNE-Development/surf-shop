package dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shopchest.SingleStaticShopChestResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class CreateStaticShopChestRequestPacket(
    val shopUuid: SerializableUUID?,
    val placedBy: SerializableUUID,
    val worldName: String,
    val x: Int,
    val y: Int,
    val z: Int
) : RabbitRequestPacket<SingleStaticShopChestResponsePacket>()
