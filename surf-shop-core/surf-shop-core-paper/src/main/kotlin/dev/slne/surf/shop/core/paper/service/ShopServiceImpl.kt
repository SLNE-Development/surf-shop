package dev.slne.surf.shop.core.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.CreateShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.DeleteShopRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.LoadShopsRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.shop.SaveShopRequestPacket
import dev.slne.surf.shop.core.common.service.ShopMutations
import dev.slne.surf.shop.core.common.service.ShopSaveRejectedException
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StockChangeResult
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.util.rebuildSearchTokens
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

    /** Per-shop locks, quarantine after unknown saves and the blocked state (see [ShopMutations]). */
    private val mutations = ShopMutations(
        currentShop = { _shops[it] },
        persist = { saveShop(it) }
    )

    override suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Double,
        seller: UUID
    ): Shop {
        require(ShopPriceRules.isValidPrice(pricePerItem)) { "Invalid shop price: $pricePerItem" }

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
        mutations.block(shop.shopUuid)
        _shops[shop.shopUuid]?.let { it.isBlocked = true }
    }

    override fun unblockShop(shop: Shop) {
        mutations.unblock(shop.shopUuid)
        _shops[shop.shopUuid]?.let { it.isBlocked = false }
    }

    /**
     * Persists [shop] first and only updates the local cache once the backend confirmed the save,
     * so a failed save never leaves the cache ahead of the database.
     *
     * @throws ShopSaveRejectedException if the save was definitely not persisted; any other exception
     * means the outcome is unknown
     */
    override suspend fun saveShop(shop: Shop): Shop {
        if (!ShopPriceRules.isValidPrice(shop.pricePerItem)) {
            throw ShopSaveRejectedException("Invalid shop price: ${shop.pricePerItem} (shop ${shop.shopUuid})")
        }

        val updatedShop = shop.apply {
            rebuildSearchTokens()
        }

        val saved = PaperShopInstance.rabbitApi.sendRequest(
            SaveShopRequestPacket(
                updatedShop
            )
        ).value
        if (!saved) throw ShopSaveRejectedException("Backend rejected saving shop ${shop.shopUuid}")

        putShop(updatedShop)
        return updatedShop
    }

    override suspend fun changeStock(
        shopUuid: UUID,
        delta: Int,
        allowPartialRemoval: Boolean
    ): StockChangeResult = mutations.changeStock(shopUuid, delta, allowPartialRemoval)

    override suspend fun updatePrice(shopUuid: UUID, pricePerItem: Double): Shop? =
        mutations.updatePrice(shopUuid, pricePerItem)

    override suspend fun deleteShop(shop: Shop): Boolean {
        return mutations.lock(shop.shopUuid).withLock {

            val deleted = PaperShopInstance.rabbitApi.sendRequest(
                DeleteShopRequestPacket(
                    shop
                )
            ).value

            if (deleted) {
                removeShop(shop)
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

        // The database is authoritative again; shops quarantined after unknown saves are usable.
        val released = mutations.clearQuarantine()
        if (released > 0) logger.info("Released $released quarantined shop(s) after reload")
    }

    private fun putShop(shop: Shop) {
        shop.isBlocked = mutations.isBlocked(shop.shopUuid)

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
