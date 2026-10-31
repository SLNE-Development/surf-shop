package dev.slne.surf.shop.core.common.service

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.StockCalc
import dev.slne.surf.shop.core.common.util.StockMath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Serializes stock and price changes per shop and keeps state that must survive snapshot replacement.
 *
 * - Every change reads the **current** shop and runs under that shop's lock.
 * - A save with an unknown outcome (anything but [ShopSaveRejectedException]) **quarantines** the shop:
 *   further changes are rejected until [clearQuarantine] (after reloading shops from the database),
 *   so a stale cached stock can never overwrite the real one.
 * - The blocked flag lives here, not in replaceable [Shop] snapshots; saved shops carry the state
 *   that is current when the save completes.
 *
 * @param currentShop returns the currently cached shop
 * @param persist persists and publishes a shop; throws [ShopSaveRejectedException] if it was definitely
 * not saved, any other exception if the outcome is unknown
 */
class ShopMutations(
    private val currentShop: (UUID) -> Shop?,
    private val persist: suspend (Shop) -> Shop
) {
    private val locks = ConcurrentHashMap<UUID, Mutex>()
    private val quarantined: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val blocked: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    /** The shop's lock; never removed, so all callers always share one instance per shop. */
    fun lock(shopUuid: UUID): Mutex = locks.computeIfAbsent(shopUuid) { Mutex() }

    fun isBlocked(shopUuid: UUID): Boolean = shopUuid in blocked
    fun block(shopUuid: UUID) {
        blocked += shopUuid
    }

    fun unblock(shopUuid: UUID) {
        blocked -= shopUuid
    }

    fun isQuarantined(shopUuid: UUID): Boolean = shopUuid in quarantined

    /** Lifts all quarantines; call only after the cache was reloaded from the database. */
    fun clearQuarantine(): Int {
        val count = quarantined.size
        quarantined.clear()
        return count
    }

    suspend fun changeStock(shopUuid: UUID, delta: Int, allowPartialRemoval: Boolean): StockChangeResult =
        lock(shopUuid).withLock {
            ensureNotQuarantined(shopUuid)
            val current = currentShop(shopUuid) ?: return@withLock StockChangeResult.NotFound

            when (val calc = StockMath.apply(current.storedItemCount, delta, allowPartialRemoval)) {
                is StockCalc.Insufficient -> StockChangeResult.InsufficientStock(calc.available)
                StockCalc.Overflow -> StockChangeResult.Overflow
                is StockCalc.Ok -> StockChangeResult.Changed(
                    save(current.copy(storedItemCount = calc.newCount)),
                    calc.appliedDelta
                )
            }
        }

    suspend fun updatePrice(shopUuid: UUID, pricePerItem: Double): Shop? {
        require(ShopPriceRules.isValidPrice(pricePerItem)) { "Invalid shop price: $pricePerItem" }

        return lock(shopUuid).withLock {
            ensureNotQuarantined(shopUuid)
            val current = currentShop(shopUuid) ?: return@withLock null
            save(current.copy(pricePerItem = pricePerItem))
        }
    }

    private suspend fun save(shop: Shop): Shop {
        val saved = try {
            persist(shop)
        } catch (e: ShopSaveRejectedException) {
            throw e
        } catch (e: Throwable) {
            quarantined += shop.shopUuid
            throw e
        }

        return saved.apply { isBlocked = isBlocked(shopUuid) }
    }

    private fun ensureNotQuarantined(shopUuid: UUID) {
        if (shopUuid in quarantined) {
            throw ShopSaveRejectedException(
                "Shop $shopUuid is quarantined after a save with unknown outcome; reload shops to reconcile"
            )
        }
    }
}
