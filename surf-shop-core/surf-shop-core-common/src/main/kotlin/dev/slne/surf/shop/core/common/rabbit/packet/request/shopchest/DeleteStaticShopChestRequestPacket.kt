package dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class DeleteStaticShopChestRequestPacket(
    val chestUuid: SerializableUUID
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
