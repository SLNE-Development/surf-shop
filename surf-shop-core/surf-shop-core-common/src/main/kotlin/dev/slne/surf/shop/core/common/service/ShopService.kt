package dev.slne.surf.shop.core.service

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.inventory.ItemStack
import java.util.*

val shopService = requiredService<ShopService>()

interface ShopService {
    val loadedShops: ObjectSet<Shop>

    suspend fun createShop(
        item: ItemStack,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Shop

    fun blockShop(shop: Shop)
    fun unblockShop(shop: Shop)

    suspend fun saveShop(shop: Shop): Shop
    suspend fun deleteShop(shop: Shop): Boolean
    suspend fun fetchShops()
}