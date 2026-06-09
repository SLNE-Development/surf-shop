package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.shop.Shop
import java.util.*

private val service = requiredService<ShopService>()

interface ShopService {
    val loadedShops: Collection<Shop>

    fun loadedShopsSnapshot(): List<Shop> = loadedShops.toList()
    fun getShop(shopUuid: UUID): Shop?
    fun getShopByInternalId(internalId: ULong): Shop?

    suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Double,
        seller: UUID
    ): Shop

    fun blockShop(shop: Shop)
    fun unblockShop(shop: Shop)

    suspend fun saveShop(shop: Shop): Shop
    suspend fun deleteShop(shop: Shop): Boolean
    suspend fun fetchShops()

    companion object : ShopService by service
}
