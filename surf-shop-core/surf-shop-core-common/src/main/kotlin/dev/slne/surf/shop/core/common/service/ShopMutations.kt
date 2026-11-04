package dev.slne.surf.shop.core.common.service

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.StockCalc
import dev.slne.surf.shop.core.common.util.StockMath
import dev.slne.surf.shop.core.common.util.logger
import kotlinx.coroutines.CancellationException
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
            if (shopUuid in quarantined) return@withLock StockChangeResult.Rejected(quarantineReason(shopUuid))
            val current = currentShop(shopUuid) ?: return@withLock StockChangeResult.NotFound

            when (val calc = StockMath.apply(current.storedItemCount, delta, allowPartialRemoval)) {
                is StockCalc.Insufficient -> StockChangeResult.InsufficientStock(calc.available)
                StockCalc.Overflow -> StockChangeResult.Overflow
                is StockCalc.Ok -> when (val outcome = save(current.copy(storedItemCount = calc.newCount))) {
                    is SaveOutcome.Saved -> StockChangeResult.Changed(outcome.shop, calc.appliedDelta)
                    is SaveOutcome.Rejected -> StockChangeResult.Rejected(outcome.reason)
                    is SaveOutcome.Unknown -> StockChangeResult.Unknown(outcome.cause)
                }
            }
        }

    suspend fun updatePrice(shopUuid: UUID, pricePerItem: Double): PriceUpdateResult {
        if (!ShopPriceRules.isValidPrice(pricePerItem)) {
            return PriceUpdateResult.Rejected("Invalid shop price: $pricePerItem")
        }

        return lock(shopUuid).withLock {
            if (shopUuid in quarantined) return@withLock PriceUpdateResult.Rejected(quarantineReason(shopUuid))
            val current = currentShop(shopUuid) ?: return@withLock PriceUpdateResult.NotFound

            when (val outcome = save(current.copy(pricePerItem = pricePerItem))) {
                is SaveOutcome.Saved -> PriceUpdateResult.Updated(outcome.shop)
                is SaveOutcome.Rejected -> PriceUpdateResult.Rejected(outcome.reason)
                is SaveOutcome.Unknown -> PriceUpdateResult.Unknown(outcome.cause)
            }
        }
    }

    private sealed interface SaveOutcome {
        data class Saved(val shop: Shop) : SaveOutcome
        data class Rejected(val reason: String) : SaveOutcome
        data class Unknown(val cause: Throwable) : SaveOutcome
    }

    /** Classifies a save; cancellation is rethrown (after quarantining, as its outcome is unknown). */
    private suspend fun save(shop: Shop): SaveOutcome {
        val saved = try {
            persist(shop)
        } catch (e: ShopSaveRejectedException) {
            return SaveOutcome.Rejected(e.message ?: "rejected")
        } catch (e: CancellationException) {
            quarantined += shop.shopUuid
            // The quarantine lives in memory only (e.g. lost on a server stop), so leave a trace to reconcile.
            val cached = currentShop(shop.shopUuid)
            logger.severe(
                "MANUAL RECONCILIATION REQUIRED: saving shop ${shop.shopUuid} was cancelled before its outcome " +
                        "was known. The database holds either the previous state (stock ${cached?.storedItemCount}, " +
                        "price ${cached?.pricePerItem}) or the new one (stock ${shop.storedItemCount}, " +
                        "price ${shop.pricePerItem}); verify the shop."
            )
            throw e
        } catch (e: Exception) {
            quarantined += shop.shopUuid
            return SaveOutcome.Unknown(e)
        }

        return SaveOutcome.Saved(saved.apply { isBlocked = isBlocked(shopUuid) })
    }

    private fun quarantineReason(shopUuid: UUID) =
        "Shop $shopUuid is quarantined after a save with unknown outcome; reload shops to reconcile"
}
