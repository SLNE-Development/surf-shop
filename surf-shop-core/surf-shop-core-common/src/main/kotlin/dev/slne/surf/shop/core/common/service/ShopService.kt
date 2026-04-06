package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.shop.Shop
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

private val service = requiredService<ShopService>()

interface ShopService {
    val loadedShops: ObjectSet<Shop>

    suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Shop

    fun blockShop(shop: Shop)
    fun unblockShop(shop: Shop)

    suspend fun saveShop(shop: Shop): Shop
    suspend fun deleteShop(shop: Shop): Boolean
    suspend fun fetchShops()

    companion object : ShopService by service
}