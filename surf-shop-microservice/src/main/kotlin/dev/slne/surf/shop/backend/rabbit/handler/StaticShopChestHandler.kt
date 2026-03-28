package dev.slne.surf.shop.backend.rabbit.handler

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.shop.backend.repository.staticShopChestRepository
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.CreateStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.DeleteStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.LoadStaticShopChestsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.UpdateStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shopchest.ManyStaticShopChestsResponsePacket
import dev.slne.surf.shop.core.common.rabbit.packet.response.shopchest.SingleStaticShopChestResponsePacket
import kotlinx.coroutines.launch

object StaticShopChestHandler {
    @RabbitHandler
    fun handleLoadStaticShopChestsRequestPacket(packet: LoadStaticShopChestsRequestPacket) =
        packet.launch {
            packet.respond(ManyStaticShopChestsResponsePacket(staticShopChestRepository.loadAll()))
        }

    @RabbitHandler
    fun handleCreateStaticShopChestRequestPacket(packet: CreateStaticShopChestRequestPacket) =
        packet.launch {
            packet.respond(
                SingleStaticShopChestResponsePacket(
                    staticShopChestRepository.create(
                        packet.shopUuid,
                        packet.placedBy,
                        packet.worldName,
                        packet.x,
                        packet.y,
                        packet.z
                    )
                )
            )
        }

    @RabbitHandler
    fun handleUpdateStaticShopChestRequestPacket(packet: UpdateStaticShopChestRequestPacket) =
        packet.launch {
            staticShopChestRepository.updateShopUuid(packet.chestUuid, packet.shopUuid)
            packet.respond(PrimitiveResponse.BooleanResponsePacket(true))
        }

    @RabbitHandler
    fun handleDeleteStaticShopChestRequestPacket(packet: DeleteStaticShopChestRequestPacket) =
        packet.launch {
            packet.respond(
                PrimitiveResponse.BooleanResponsePacket(
                    staticShopChestRepository.delete(packet.chestUuid)
                )
            )
        }
}
