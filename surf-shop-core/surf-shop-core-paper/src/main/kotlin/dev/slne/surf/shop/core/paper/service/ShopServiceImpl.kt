package dev.slne.surf.shop.core.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.CreateShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.DeleteShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.LoadShopsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.SaveShopRequestPacket
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.util.rebuildSearchTokens
import dev.slne.surf.shop.core.service.ShopService
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

@AutoService(ShopService::class)
class ShopServiceImpl : ShopService, Services.Fallback {

    private val _shops = mutableObject2ObjectMapOf<UUID, Shop>()
    override val loadedShops: ObjectSet<Shop> get() = _shops.values.toObjectSet()

    private val shopLocks = ConcurrentHashMap<UUID, Mutex>()

    private fun getLock(shopUuid: UUID): Mutex {
        return shopLocks.computeIfAbsent(shopUuid) { Mutex() }
    }

    override suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Shop {

        val created = PaperShopInstance.rabbitApi.sendRequest(
            CreateShopRequestPacket(
                itemString,
                storedItemCount,
                pricePerItem,
                seller,
                OffsetDateTime.now()
            )
        ).shop.apply {
            rebuildSearchTokens()
        }

        _shops[created.shopUuid] = created
        return created
    }

    override fun blockShop(shop: Shop) {
        _shops[shop.shopUuid]?.let { it.isBlocked = true }
    }

    override fun unblockShop(shop: Shop) {
        _shops[shop.shopUuid]?.let { it.isBlocked = false }
    }

    override suspend fun saveShop(shop: Shop): Shop {
        val updatedShop = shop.apply {
            rebuildSearchTokens()
        }

        _shops[shop.shopUuid] = updatedShop

        PaperShopInstance.rabbitApi.sendRequest(
            SaveShopRequestPacket(
                updatedShop
            )
        )

        return updatedShop
    }

    override suspend fun deleteShop(shop: Shop): Boolean {
        val lock = getLock(shop.shopUuid)

        return lock.withLock {

            val deleted = PaperShopInstance.rabbitApi.sendRequest(
                DeleteShopRequestPacket(
                    shop
                )
            ).value

            if (deleted) {
                _shops.remove(shop.shopUuid)
                shopLocks.remove(shop.shopUuid)
            }

            deleted
        }
    }

    override suspend fun fetchShops() {

        logger.info("Fetching shops from database... (this may take a while!)")

        val ms = measureTimeMillis {

            val loadedShops = PaperShopInstance.rabbitApi.sendRequest(LoadShopsRequestPacket).shops

            _shops.clear()

            loadedShops.forEach {
                _shops[it.shopUuid] = it.apply {
                    rebuildSearchTokens()
                }
            }
        }

        logger.info("Loaded ${_shops.size} shops in ${ms}ms")
    }
}