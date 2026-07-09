package dev.slne.surf.shop.core.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.CreateShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.DeleteShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.LoadShopsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.SaveShopRequestPacket
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.util.rebuildSearchTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

@AutoService(ShopService::class)
class ShopServiceImpl : ShopService, Services.Fallback {

    private val _shops = ConcurrentHashMap<UUID, Shop>()
    private val _shopsByInternalId = ConcurrentHashMap<ULong, Shop>()

    override val loadedShops: Collection<Shop> get() = _shops.values

    override fun getShop(shopUuid: UUID) = _shops[shopUuid]
    override fun getShopByInternalId(internalId: ULong) = _shopsByInternalId[internalId]

    private val shopLocks = ConcurrentHashMap<UUID, Mutex>()

    private fun getLock(shopUuid: UUID): Mutex {
        return shopLocks.computeIfAbsent(shopUuid) { Mutex() }
    }

    override suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Double,
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

        putShop(created)
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

        putShop(updatedShop)

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
                removeShop(shop)
                shopLocks.remove(shop.shopUuid)
            }

            deleted
        }
    }

    override suspend fun fetchShops() {

        logger.info("Fetching shops from database... (this may take a while!)")

        val ms = measureTimeMillis {

            val loadedShops =
                PaperShopInstance.rabbitApi.sendRequest(LoadShopsRequestPacket()).shops

            _shops.clear()
            _shopsByInternalId.clear()

            loadedShops.forEach {
                putShop(it.apply {
                    rebuildSearchTokens()
                })
            }
        }

        logger.info("Loaded ${_shops.size} shops in ${ms}ms")
    }

    private fun putShop(shop: Shop) {
        _shops[shop.shopUuid]?.let { existing ->
            if (existing.internalId != shop.internalId) {
                _shopsByInternalId.remove(existing.internalId)
            }
        }

        _shops[shop.shopUuid] = shop
        _shopsByInternalId[shop.internalId] = shop
    }

    private fun removeShop(shop: Shop) {
        val removed = _shops.remove(shop.shopUuid)
        _shopsByInternalId.remove(removed?.internalId ?: shop.internalId)
    }
}
