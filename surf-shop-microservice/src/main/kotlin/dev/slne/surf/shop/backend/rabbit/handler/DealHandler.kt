package dev.slne.surf.shop.backend.rabbit.handler

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.shop.backend.repository.dealRepository
import dev.slne.surf.shop.core.common.rabbit.packet.request.deal.BuyRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.deal.LoadDealsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.deal.ManyDealsResponsePacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.deal.SingleDealResponsePacket
import kotlinx.coroutines.launch

object DealHandler {
    @RabbitHandler
    fun handleBuyRequestPacket(packet: BuyRequestPacket) = packet.launch {
        packet.respond(
            SingleDealResponsePacket(
                dealRepository.buy(
                    packet.shop,
                    packet.amount,
                    packet.buyer,
                    packet.boughtAt
                )
            )
        )
    }

    @RabbitHandler
    fun handleLoadDealsRequestPacket(packet: LoadDealsRequestPacket) = packet.launch {
        packet.respond(ManyDealsResponsePacket(dealRepository.loadDeals()))
    }
}