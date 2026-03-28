package dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class UpdateStaticShopChestRequestPacket(
    val chestUuid: SerializableUUID,
    val shopUuid: SerializableUUID?
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
