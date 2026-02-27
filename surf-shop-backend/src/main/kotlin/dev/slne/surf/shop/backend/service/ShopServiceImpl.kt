package dev.slne.surf.shop.backend.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.backend.repository.shopRepository
import dev.slne.surf.shop.core.service.ShopService
import dev.slne.surf.shop.core.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import org.bukkit.inventory.ItemStack
import java.time.OffsetDateTime
import java.util.*
import kotlin.system.measureTimeMillis

@AutoService(ShopService::class)
class ShopServiceImpl : ShopService, Services.Fallback {
    private val _shops = mutableObject2ObjectMapOf<UUID, Shop>()
    override val loadedShops: ObjectSet<Shop> get() = _shops.values.toObjectSet()

    override suspend fun createShop(
        item: ItemStack,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Shop {
        val createdByRepository = shopRepository.createShop(
            item,
            storedItemCount,
            pricePerItem,
            seller,
            OffsetDateTime.now()
        ).apply {
            this.rebuildSearchTokens()
        }

        _shops[createdByRepository.shopUuid] = createdByRepository
        return createdByRepository
    }

    override fun blockShop(shop: Shop) {
        _shops[shop.shopUuid] = shop.apply {
            isBlocked = true
        }
    }

    override fun unblockShop(shop: Shop) {
        _shops[shop.shopUuid] = shop.apply {
            isBlocked = false
        }
    }

    override suspend fun saveShop(shop: Shop): Shop {
        val updatedShop = shop.apply {
            this.rebuildSearchTokens()
        }

        _shops[shop.shopUuid] = updatedShop
        shopRepository.saveShop(updatedShop)
        return shop
    }

    override suspend fun deleteShop(shop: Shop): Boolean {
        val deletedByRepository = shopRepository.deleteShop(shop)
        if (deletedByRepository) {
            _shops.remove(shop.shopUuid)
        }
        return deletedByRepository
    }

    override suspend fun fetchShops() {
        logger.info("Fetching shops from database... (this may take a while!)")

        val ms = measureTimeMillis {
            val loadedShops = shopRepository.loadShops()

            _shops.clear()
            loadedShops.forEach {
                _shops[it.shopUuid] = it.apply {
                    this.rebuildSearchTokens()
                }
            }
        }

        logger.info("Loaded ${_shops.size} shops in ${ms}ms")
    }
}