package dev.slne.surf.shop.core.common.rabbit.packet.request.deal

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.deal.ManyDealsResponsePacket
import kotlinx.serialization.Serializable

@Serializable
class LoadDealsRequestPacket : RabbitRequestPacket<ManyDealsResponsePacket>()
