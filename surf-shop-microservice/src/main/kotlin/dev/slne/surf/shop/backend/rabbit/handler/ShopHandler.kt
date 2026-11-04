package dev.slne.surf.shop.backend.rabbit.handler

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.shop.backend.repository.shopRepository
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.CreateShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.DeleteShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.LoadShopsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.SaveShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shop.ManyShopsResponsePacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shop.SingleShopResponsePacket
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.logger
import kotlinx.coroutines.launch

object ShopHandler {
    @RabbitHandler
    fun handleLoadShopsRequestPacket(packet: LoadShopsRequestPacket) = packet.launch {
        packet.respond(ManyShopsResponsePacket(shopRepository.loadShops()))
    }

    @RabbitHandler
    fun handleSaveShopRequestPacket(packet: SaveShopRequestPacket) = packet.launch {
        val shop = packet.shop

        if (!ShopPriceRules.isValidPrice(shop.pricePerItem)) {
            logger.warning("Rejected saving shop ${shop.shopUuid}: invalid price ${shop.pricePerItem}")
            packet.respond(PrimitiveResponse.BooleanResponsePacket(false))
            return@launch
        }

        shopRepository.saveShop(shop)
        packet.respond(PrimitiveResponse.BooleanResponsePacket(true))
    }

    @RabbitHandler
    fun handleCreateShopRequestPacket(packet: CreateShopRequestPacket) = packet.launch {
        if (!ShopPriceRules.isValidPrice(packet.pricePerItem)) {
            logger.warning("Rejected creating shop for ${packet.seller}: invalid price ${packet.pricePerItem}")
            throw IllegalArgumentException("Invalid shop price: ${packet.pricePerItem}")
        }

        packet.respond(
            SingleShopResponsePacket(
                shopRepository.createShop(
                    packet.itemString,
                    packet.storedItemCount,
                    packet.pricePerItem,
                    packet.seller,
                    packet.createdAt
                )
            )
        )
    }

    @RabbitHandler
    fun handleDeleteShopRequestPacket(packet: DeleteShopRequestPacket) = packet.launch {
        packet.respond(PrimitiveResponse.BooleanResponsePacket(shopRepository.deleteShop(packet.shop)))
    }
}