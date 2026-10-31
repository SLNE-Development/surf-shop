package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.shop.Shop
import java.util.*

private val service = requiredService<ShopService>()

/** Result of [ShopService.changeStock]. */
sealed interface StockChangeResult {
    /** Stock was changed and persisted; [appliedDelta] can be smaller than requested for partial removals. */
    data class Changed(val shop: Shop, val appliedDelta: Int) : StockChangeResult
    data object NotFound : StockChangeResult
    data class InsufficientStock(val available: Int) : StockChangeResult
    data object Overflow : StockChangeResult
}

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

    /**
     * Changes the **current** stored item count of a shop by [delta] under the shop's lock and persists it.
     * Use this instead of saving a copied shop, so concurrent changes never overwrite each other.
     */
    suspend fun changeStock(shopUuid: UUID, delta: Int, allowPartialRemoval: Boolean = false): StockChangeResult

    /**
     * Changes only the price of the **current** shop under the shop's lock and persists it.
     *
     * @return the updated shop, or `null` if the shop no longer exists
     * @throws IllegalArgumentException if [pricePerItem] is not a valid price
     */
    suspend fun updatePrice(shopUuid: UUID, pricePerItem: Double): Shop?

    suspend fun deleteShop(shop: Shop): Boolean
    suspend fun fetchShops()

    companion object : ShopService by service
}
