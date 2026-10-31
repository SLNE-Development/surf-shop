package dev.slne.surf.shop.core.paper.service

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.rabbit.packet.request.deal.BuyRequestPacket
import dev.slne.surf.shop.core.common.rabbit.packet.request.deal.LoadDealsRequestPacket
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopDealStats
import dev.slne.surf.shop.core.common.service.ShopSaveRejectedException
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StockChangeResult
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import dev.slne.surf.transaction.api.user.TransactionUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import kotlin.time.Duration.Companion.seconds

@AutoService(DealService::class)
class DealServiceImpl : DealService, Services.Fallback {
    private val _deals = ConcurrentHashMap<UUID, Deal>()
    private val _dealStatsByShop = ConcurrentHashMap<ULong, ShopDealStats>()

    override val loadedDeals: Collection<Deal> get() = _deals.values

    override fun getDealStats(shopInternalId: ULong) =
        _dealStatsByShop[shopInternalId] ?: ShopDealStats()

    private val shopLocks = ConcurrentHashMap<UUID, Mutex>()

    private fun getLock(shopUuid: UUID): Mutex {
        return shopLocks.computeIfAbsent(shopUuid) { Mutex() }
    }

    override suspend fun buyInternal(
        shop: Shop,
        amount: Int,
        buyer: UUID
    ): Deal {
        val bought = PaperShopInstance.rabbitApi.sendRequest(
            BuyRequestPacket(
                shop,
                amount,
                buyer,
                OffsetDateTime.now()
            )
        ).deal

        putDeal(bought)
        return bought
    }

    /** Outcome of the locked part of a purchase (reserve → stock → deal → commit). */
    private sealed interface PurchaseOutcome {
        data class Committed(
            val deal: Deal,
            val shop: Shop,
            val amount: Int,
            val total: BigDecimal,
            val transactionId: UUID
        ) : PurchaseOutcome

        data class Failed(val result: Deal.DealResult) : PurchaseOutcome
    }

    /**
     * Buys [amount] items (or what is left in stock) from [shop] for the player.
     *
     * The shop price the player saw ([shop]) must still be the current price; otherwise the purchase
     * is refused. Money is reserved first, then stock is reduced and the deal recorded, and only then
     * the payment is committed. Items are delivered after a successful commit. If the payment outcome
     * is unknown, no items are delivered and the case is logged for manual reconciliation.
     */
    override suspend fun buy(
        playerUuid: UUID,
        shop: Shop,
        amount: Int
    ): Deal.DealResult {
        val player = Bukkit.getPlayer(playerUuid) ?: return Deal.DealResult.PlayerNotFound
        if (amount < 1) return Deal.DealResult.TransactionFailed

        val outcome = getLock(shop.shopUuid).withLock {
            purchaseLocked(player, shop, amount)
        }

        return when (outcome) {
            is PurchaseOutcome.Failed -> outcome.result
            is PurchaseOutcome.Committed -> {
                putDeal(outcome.deal)
                val delivered = deliverItems(player, outcome)
                collectTax(outcome)

                // A paid purchase whose delivery failed must not be reported as success;
                // the buyer gets the support message and the case is logged for reconciliation.
                if (delivered) Deal.DealResult.Success(outcome.deal) else Deal.DealResult.TransactionFailed
            }
        }
    }

    private suspend fun purchaseLocked(player: Player, quotedShop: Shop, amount: Int): PurchaseOutcome {
        // Re-read under the lock: the snapshot the caller holds may be outdated.
        val shop = ShopService.getShop(quotedShop.shopUuid)
            ?: return PurchaseOutcome.Failed(Deal.DealResult.ShopDeleted)

        if (shop.isBlocked) return PurchaseOutcome.Failed(Deal.DealResult.ShopBlocked)

        if (shop.pricePerItem != quotedShop.pricePerItem) {
            logger.info(
                "Refused purchase in shop ${shop.shopUuid} by ${player.uniqueId}: price changed " +
                        "from ${quotedShop.pricePerItem} to ${shop.pricePerItem}"
            )
            return PurchaseOutcome.Failed(Deal.DealResult.ShopBlocked)
        }

        if (!ShopPriceRules.isValidPrice(shop.pricePerItem)) {
            return PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
        }

        if (shop.storedItemCount <= 0) return PurchaseOutcome.Failed(Deal.DealResult.InsufficientStock)

        val buyAmount = minOf(shop.storedItemCount, amount)
        val total = ShopPriceRules.totalPrice(shop.pricePerItem, buyAmount)
        if (!ShopPriceRules.isAllowedTotal(total)) {
            return PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
        }

        val result = try {
            TransactionUser[player.uniqueId].withPendingTransferDecision(
                amount = total,
                currency = Currency.default(),
                receiver = TransactionUser[shop.seller].getDefaultAccount(),
                additionalSenderData = objectSetOf(
                    TransactionData.of("reason", "bought item"),
                    TransactionData.of(
                        "shopData",
                        "$buyAmount x ${shop.item} for ${shop.pricePerItem} each (total: $total)"
                    )
                ),
                additionalReceiverData = objectSetOf(
                    TransactionData.of("reason", "sold item"),
                    TransactionData.of(
                        "shopData",
                        "$buyAmount x ${shop.item} for ${shop.pricePerItem} by ${player.name} (total: $total)"
                    )
                ),
                // The block handles its own failures and returns an explicit decision.
                rollbackOn = PendingRollbackPolicy.Never
            ) { transactionId ->
                prepareDeal(player, shop, buyAmount, transactionId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(
                Level.WARNING,
                "Purchase reservation failed for ${player.uniqueId} in shop ${shop.shopUuid} " +
                        "($buyAmount items, total $total); an existing reservation expires automatically",
                e
            )
            return PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
        }

        return when (result) {
            is PendingExecutionResult.Completed -> {
                when (val value = result.value) {
                    is Deal.DealResult.Success -> PurchaseOutcome.Committed(
                        deal = value.deal,
                        shop = shop,
                        amount = buyAmount,
                        total = total,
                        transactionId = result.transaction.identifier
                    )

                    else -> PurchaseOutcome.Failed(value)
                }
            }

            is PendingExecutionResult.ReservationFailed -> PurchaseOutcome.Failed(
                when (result.result) {
                    PendingTransactionResult.SenderInsufficientFunds -> Deal.DealResult.SelfInsufficientFounds
                    PendingTransactionResult.ReceiverInsufficientFunds -> Deal.DealResult.OtherInsufficientFounds
                    else -> {
                        logger.warning("Purchase reservation rejected for ${player.uniqueId} in shop ${shop.shopUuid}: ${result.result}")
                        Deal.DealResult.TransactionFailed
                    }
                }
            )

            is PendingExecutionResult.RolledBack -> PurchaseOutcome.Failed(result.value)

            is PendingExecutionResult.RollbackFailed -> {
                logger.warning(
                    "Rollback of purchase reservation ${result.transaction.identifier} failed " +
                            "(buyer ${player.uniqueId}, shop ${shop.shopUuid}): ${result.failure}; " +
                            "the reservation expires automatically"
                )
                PurchaseOutcome.Failed(result.value)
            }

            is PendingExecutionResult.ExternalFailureRolledBack -> {
                logger.log(
                    Level.WARNING,
                    "Purchase ${result.transaction.identifier} rolled back after failure",
                    result.cause
                )
                PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
            }

            is PendingExecutionResult.ExternalFailureRollbackFailed -> {
                logger.log(
                    Level.SEVERE,
                    "Purchase ${result.transaction.identifier} failed and its rollback failed too " +
                            "(buyer ${player.uniqueId}, shop ${shop.shopUuid}): ${result.failure}",
                    result.cause
                )
                PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
            }

            is PendingExecutionResult.CommitFailed -> {
                val deal = (result.value as? Deal.DealResult.Success)?.deal
                logger.severe(
                    "MANUAL RECONCILIATION REQUIRED: payment ${result.transaction.identifier} is in doubt " +
                            "(${result.failure}). Buyer ${player.name} (${player.uniqueId}) did NOT receive " +
                            "$buyAmount x ${shop.item} from shop ${shop.shopUuid}; total $total; stock stays " +
                            "reduced by $buyAmount; deal ${deal?.dealUuid}. Check the transaction and either " +
                            "hand out the items or restore the stock."
                )
                PurchaseOutcome.Failed(Deal.DealResult.TransactionFailed)
            }
        }
    }

    /**
     * Runs while the payment is reserved: reduce stock, record the deal, decide commit or rollback.
     * Never throws except for cancellation; every failure is classified and rolled back explicitly.
     */
    private suspend fun prepareDeal(
        player: Player,
        shop: Shop,
        buyAmount: Int,
        transactionId: UUID
    ): PendingExecutionDecision<Deal.DealResult> {
        if (!player.isOnline) return PendingExecutionDecision.Rollback(Deal.DealResult.PlayerNotFound)

        val stock = try {
            ShopService.changeStock(shop.shopUuid, -buyAmount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ShopSaveRejectedException) {
            logger.log(Level.WARNING, "Stock reduction for purchase $transactionId was rejected", e)
            return PendingExecutionDecision.Rollback(Deal.DealResult.TransactionFailed)
        } catch (e: Exception) {
            logger.log(
                Level.SEVERE,
                "Stock reduction for purchase $transactionId has an unknown outcome; payment is rolled back. " +
                        "Verify the stock of shop ${shop.shopUuid} (possible reduction by $buyAmount).",
                e
            )
            return PendingExecutionDecision.Rollback(Deal.DealResult.TransactionFailed)
        }

        when (stock) {
            StockChangeResult.NotFound -> return PendingExecutionDecision.Rollback(Deal.DealResult.ShopDeleted)
            is StockChangeResult.InsufficientStock ->
                return PendingExecutionDecision.Rollback(Deal.DealResult.InsufficientStock)

            StockChangeResult.Overflow -> return PendingExecutionDecision.Rollback(Deal.DealResult.TransactionFailed)
            is StockChangeResult.Changed -> Unit
        }

        val deal = try {
            PaperShopInstance.rabbitApi.sendRequest(
                BuyRequestPacket(
                    stock.shop,
                    buyAmount,
                    player.uniqueId,
                    OffsetDateTime.now()
                )
            ).deal
        } catch (e: CancellationException) {
            logger.warning(
                "Purchase $transactionId was cancelled after the stock of shop ${shop.shopUuid} was reduced " +
                        "by $buyAmount; restoring stock. The payment reservation expires automatically."
            )
            withContext(NonCancellable) {
                withTimeoutOrNull(STOCK_RESTORE_TIMEOUT) { restoreStock(shop, buyAmount, transactionId) }
                    ?: logger.severe(
                        "Restoring $buyAmount items to shop ${shop.shopUuid} after cancelled purchase " +
                                "$transactionId timed out. Verify the stock manually."
                    )
            }
            throw e
        } catch (e: Exception) {
            logger.log(
                Level.WARNING,
                "Recording the deal for purchase $transactionId failed; payment is rolled back and stock " +
                        "restored. A deal row may still exist for this aborted purchase.",
                e
            )
            restoreStock(shop, buyAmount, transactionId)
            return PendingExecutionDecision.Rollback(Deal.DealResult.TransactionFailed)
        }

        if (!player.isOnline) {
            logger.info(
                "Buyer ${player.uniqueId} left before purchase $transactionId was committed; payment is " +
                        "rolled back and stock restored. Deal ${deal.dealUuid} belongs to the aborted purchase."
            )
            restoreStock(shop, buyAmount, transactionId)
            return PendingExecutionDecision.Rollback(Deal.DealResult.PlayerNotFound)
        }

        return PendingExecutionDecision.Commit(Deal.DealResult.Success(deal))
    }

    private suspend fun restoreStock(shop: Shop, amount: Int, transactionId: UUID) {
        try {
            val restored = ShopService.changeStock(shop.shopUuid, amount)
            if (restored !is StockChangeResult.Changed) {
                logger.severe(
                    "Could not restore $amount items to shop ${shop.shopUuid} after aborted purchase " +
                            "$transactionId: $restored. Restore the stock manually."
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(
                Level.SEVERE,
                "Could not restore $amount items to shop ${shop.shopUuid} after aborted purchase " +
                        "$transactionId. Verify the stock manually.",
                e
            )
        }
    }

    /**
     * Delivers a paid purchase. Failures are logged for manual reconciliation – the payment is final.
     *
     * @return `true` if the items were handed out (inventory or owner-tagged drops)
     */
    private suspend fun deliverItems(player: Player, purchase: PurchaseOutcome.Committed): Boolean {
        val owed = "MANUAL RECONCILIATION REQUIRED: purchase ${purchase.transactionId} is PAID " +
                "(total ${purchase.total}) but ${player.name} (${player.uniqueId}) may not have received " +
                "${purchase.amount} x ${purchase.shop.item} from shop ${purchase.shop.shopUuid}."

        if (!player.isOnline) {
            logger.severe("$owed The buyer went offline before delivery.")
            return false
        }

        try {
            // For a player who left in the meantime, MCCoroutine runs this block on the calling thread;
            // check again inside so an offline player is never counted as "delivered".
            val handedOut = withContext(plugin.entityDispatcher(player)) {
                if (!player.isOnline) return@withContext false

                val leftover = player.inventory.addItem(*buildStacks(purchase.shop.item, purchase.amount))

                if (leftover.isNotEmpty()) {
                    val dropLocation = player.location.clone()

                    withContext(plugin.regionDispatcher(dropLocation)) {
                        leftover.values.forEach {
                            dropLocation.world.dropItem(dropLocation, it).owner = player.uniqueId
                        }
                    }
                }

                true
            }

            if (!handedOut) {
                logger.severe("$owed The buyer went offline before delivery.")
                return false
            }
        } catch (e: CancellationException) {
            logger.severe("$owed Delivery was interrupted.")
            throw e
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "$owed Delivery failed.", e)
            return false
        }

        return true
    }

    private fun buildStacks(item: ItemStack, amount: Int): Array<ItemStack> {
        val maxStackSize = item.maxStackSize.coerceAtLeast(1)
        val stacks = mutableListOf<ItemStack>()
        var remaining = amount

        while (remaining > 0) {
            val stackSize = minOf(remaining, maxStackSize)
            stacks += item.clone().apply { this.amount = stackSize }
            remaining -= stackSize
        }

        return stacks.toTypedArray()
    }

    /** Withdraws the shop tax from the seller; failures never invalidate the purchase. */
    private suspend fun collectTax(purchase: PurchaseOutcome.Committed) {
        val tax = ShopPriceRules.taxFor(purchase.total)
        if (tax.signum() <= 0) return

        val seller = purchase.shop.seller
        val ratePercent = ShopPriceRules.TAX_RATE.movePointRight(2).stripTrailingZeros().toPlainString()

        try {
            val result = TransactionUser[seller].withdraw(
                tax,
                Currency.default(),
                false,
                TransactionData.of("reason", "shop-tax ($ratePercent%)")
            )

            if (result !is TransactionResult.Success) {
                logger.warning("Shop tax of $tax for purchase ${purchase.transactionId} not collected from $seller: $result")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Shop tax of $tax for purchase ${purchase.transactionId} not collected from $seller", e)
        }
    }

    override suspend fun fetchDeals() {
        logger.info("Fetching deals from database... (this may take a while!)")

        val loadedDeals = PaperShopInstance.rabbitApi.sendRequest(LoadDealsRequestPacket()).deals

        _deals.clear()
        _dealStatsByShop.clear()
        loadedDeals.forEach(::putDeal)

        logger.info("Loaded ${_deals.size} deals")
    }

    fun removeShopLock(shopUuid: UUID) {
        shopLocks.remove(shopUuid)
    }

    private fun putDeal(deal: Deal) {
        _deals[deal.dealUuid] = deal
        _dealStatsByShop.merge(
            deal.shopInternalId,
            ShopDealStats(dealCount = 1, totalSoldItems = deal.amount.toLong())
        ) { current, added ->
            ShopDealStats(
                dealCount = current.dealCount + added.dealCount,
                totalSoldItems = current.totalSoldItems + added.totalSoldItems
            )
        }
    }

    companion object {
        lateinit var plugin: SuspendingJavaPlugin

        private val STOCK_RESTORE_TIMEOUT = 10.seconds
    }
}
