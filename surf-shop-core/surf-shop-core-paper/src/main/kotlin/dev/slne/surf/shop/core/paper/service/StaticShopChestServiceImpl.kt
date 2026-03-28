package dev.slne.surf.shop.core.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.CreateStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.DeleteStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.LoadStaticShopChestsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shopchest.UpdateStaticShopChestRequestPacket
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.service.StaticShopChestService
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import java.util.*
import kotlin.system.measureTimeMillis

@AutoService(StaticShopChestService::class)
class StaticShopChestServiceImpl : StaticShopChestService, Services.Fallback {

    private val _chests = mutableObject2ObjectMapOf<UUID, StaticShopChest>()
    override val loadedChests: ObjectSet<StaticShopChest> get() = _chests.values.toObjectSet()

    override suspend fun createChest(
        shopUuid: UUID?,
        placedBy: UUID,
        worldName: String,
        x: Int,
        y: Int,
        z: Int
    ): StaticShopChest {
        val created = PaperShopInstance.rabbitApi.sendRequest(
            CreateStaticShopChestRequestPacket(
                shopUuid,
                placedBy,
                worldName,
                x,
                y,
                z
            )
        ).chest

        _chests[created.chestUuid] = created
        return created
    }

    override suspend fun updateChestShop(chestUuid: UUID, shopUuid: UUID?): StaticShopChest? {
        PaperShopInstance.rabbitApi.sendRequest(
            UpdateStaticShopChestRequestPacket(chestUuid, shopUuid)
        )

        val existing = _chests[chestUuid] ?: return null
        val updated = existing.copy(shopUuid = shopUuid)
        _chests[chestUuid] = updated
        return updated
    }

    override suspend fun deleteChest(chestUuid: UUID): Boolean {
        val deleted = PaperShopInstance.rabbitApi.sendRequest(
            DeleteStaticShopChestRequestPacket(chestUuid)
        ).value

        if (deleted) {
            _chests.remove(chestUuid)
        }

        return deleted
    }

    override suspend fun fetchChests() {
        logger.info("Fetching static shop chests from database... (this may take a while!)")

        val ms = measureTimeMillis {
            val loaded =
                PaperShopInstance.rabbitApi.sendRequest(LoadStaticShopChestsRequestPacket).chests

            _chests.clear()
            loaded.forEach {
                _chests[it.chestUuid] = it
            }
        }

        logger.info("Loaded ${_chests.size} static shop chests in ${ms}ms")
    }

    override fun getChestAt(worldName: String, x: Int, y: Int, z: Int): StaticShopChest? {
        return _chests.values.firstOrNull {
            it.worldName == worldName && it.x == x && it.y == y && it.z == z
        }
    }
}
