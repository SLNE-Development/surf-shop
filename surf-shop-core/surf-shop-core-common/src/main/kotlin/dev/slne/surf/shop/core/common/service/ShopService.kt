package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.shop.Shop
import java.util.*

private val service = requiredService<ShopService>()

/**
 * A shop save that was **definitely not persisted** (rejected locally or explicitly by the backend).
 * Any other exception from a save means the outcome is unknown (e.g. a lost response).
 */
class ShopSaveRejectedException(message: String) : IllegalStateException(message)

/** Result of [ShopService.changeStock]. */
sealed interface StockChangeResult {
    /** Stock was changed and persisted; [appliedDelta] can be smaller than requested for partial removals. */
    data class Changed(val shop: Shop, val appliedDelta: Int) : StockChangeResult
    data object NotFound : StockChangeResult
    data class InsufficientStock(val available: Int) : StockChangeResult
    data object Overflow : StockChangeResult

    /** Definitely not saved: rejected locally or by the backend, or the shop is quarantined. */
    data class Rejected(val reason: String) : StockChangeResult

    /** The save outcome is unknown (e.g. a lost response); the shop is quarantined until shops are reloaded. */
    data class Unknown(val cause: Throwable) : StockChangeResult
}

/** Result of [ShopService.updatePrice]. */
sealed interface PriceUpdateResult {
    data class Updated(val shop: Shop) : PriceUpdateResult
    data object NotFound : PriceUpdateResult

    /** Definitely not saved: invalid price, rejected by the backend, or the shop is quarantined. */
    data class Rejected(val reason: String) : PriceUpdateResult

    /** The save outcome is unknown (e.g. a lost response); the shop is quarantined until shops are reloaded. */
    data class Unknown(val cause: Throwable) : PriceUpdateResult
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
     * Never throws except for cancellation; failures are reported as [StockChangeResult.Rejected] or
     * [StockChangeResult.Unknown].
     */
    suspend fun changeStock(shopUuid: UUID, delta: Int, allowPartialRemoval: Boolean = false): StockChangeResult

    /**
     * Changes only the price of the **current** shop under the shop's lock and persists it.
     * Never throws except for cancellation.
     */
    suspend fun updatePrice(shopUuid: UUID, pricePerItem: Double): PriceUpdateResult

    suspend fun deleteShop(shop: Shop): Boolean
    suspend fun fetchShops()

    companion object : ShopService by service
}
