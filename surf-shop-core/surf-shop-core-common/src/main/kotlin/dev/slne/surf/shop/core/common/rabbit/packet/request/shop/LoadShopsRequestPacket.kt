package dev.slne.surf.shop.core.common.rabbit.packet.request.shop

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shop.ManyShopsResponsePacket
import kotlinx.serialization.Serializable

@Serializable
class LoadShopsRequestPacket : RabbitRequestPacket<ManyShopsResponsePacket>()
