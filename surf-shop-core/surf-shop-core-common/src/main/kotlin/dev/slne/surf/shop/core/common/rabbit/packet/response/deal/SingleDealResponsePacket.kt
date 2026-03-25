package dev.slne.surf.shop.core.common.rabbit.packet.response.deal

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.shop.api.deal.Deal
import kotlinx.serialization.Serializable

@Serializable
data class SingleDealResponsePacket(
    val deal: Deal
) : RabbitResponsePacket()
