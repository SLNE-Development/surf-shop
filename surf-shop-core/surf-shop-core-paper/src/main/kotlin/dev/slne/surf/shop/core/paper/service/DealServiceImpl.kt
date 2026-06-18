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
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.RoundingMode
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    private val taxRate: Double = 0.03

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

    override suspend fun buy(
        playerUuid: UUID,
        shop: Shop,
        amount: Int
    ): Deal.DealResult {
        val player = Bukkit.getPlayer(playerUuid) ?: return Deal.DealResult.PlayerNotFound

        val actualShop = ShopService.getShop(shop.shopUuid) ?: return Deal.DealResult.ShopDeleted

        val lock = getLock(actualShop.shopUuid)

        return lock.withLock {
            if (actualShop.isBlocked) {
                return@withLock Deal.DealResult.ShopBlocked
            }

            val storedAmount = actualShop.storedItemCount

            if (storedAmount == 0) {
                return@withLock Deal.DealResult.InsufficientStock
            }

            val buyAmount = if (storedAmount < amount) storedAmount else amount

            buy0(player, actualShop, buyAmount)
        }
    }

    private suspend fun buy0(
        player: Player,
        shop: Shop,
        amount: Int
    ): Deal.DealResult {
        val receiverAccount = TransactionUser[shop.seller].getDefaultAccount()
        val transactionResult = TransactionUser[player.uniqueId].transfer(
            (shop.pricePerItem * amount).toBigDecimal(),
            Currency.default(),
            receiverAccount,
            ignoreSenderMinimum = false,
            ignoreReceiverMinimum = false,
            additionalSenderData = objectSetOf(
                TransactionData.of("reason", "bought item"),
                TransactionData.of(
                    "shopData",
                    "$amount x ${shop.item} for ${shop.pricePerItem} each (total: ${shop.pricePerItem * amount})"
                )
            ),
            additionalReceiverData = objectSetOf(
                TransactionData.of("reason", "sold item"), TransactionData.of(
                    "shopData",
                    "$amount x ${shop.item} for ${shop.pricePerItem} by ${player.name} (total: ${shop.pricePerItem * amount})"
                )
            )
        )

        when (transactionResult) {
            is TransactionResult.DatabaseError -> return Deal.DealResult.TransactionFailed
            is TransactionResult.ReceiverInsufficientFunds -> return Deal.DealResult.SelfInsufficientFounds
            is TransactionResult.SenderInsufficientFunds -> return Deal.DealResult.OtherInsufficientFounds
            else -> {
                val updatedShop = shop.copy(
                    storedItemCount = shop.storedItemCount - amount
                )

                ShopService.saveShop(updatedShop)

                val deal = PaperShopInstance.rabbitApi.sendRequest(
                    BuyRequestPacket(
                        updatedShop,
                        amount,
                        player.uniqueId,
                        OffsetDateTime.now()
                    )
                ).deal

                putDeal(deal)

                val itemStack = shop.item
                var remaining = amount

                val stacks = mutableListOf<ItemStack>()

                while (remaining > 0) {
                    val stackSize = minOf(remaining, 64)
                    val stack = itemStack.clone()

                    stack.amount = stackSize
                    stacks += stack
                    remaining -= stackSize
                }

                withContext(plugin.entityDispatcher(player)) {
                    val leftover = player.inventory.addItem(*stacks.toTypedArray())

                    if (leftover.isNotEmpty()) {
                        withContext(plugin.regionDispatcher(player.location)) {
                            leftover.values.forEach {
                                player.world.dropItem(player.location, it).owner = player.uniqueId
                            }
                        }
                    }
                }

                TransactionUser[shop.seller].withdraw(
                    (shop.pricePerItem * amount * taxRate).toBigDecimal()
                        .setScale(2, RoundingMode.HALF_DOWN),
                    Currency.default(),
                    false,
                    TransactionData.of("reason", "shop-tax (${taxRate * 100}%)")
                )

                return Deal.DealResult.Success(deal)
            }
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
            ShopDealStats(dealCount = 1, totalSoldItems = deal.amount)
        ) { current, added ->
            ShopDealStats(
                dealCount = current.dealCount + added.dealCount,
                totalSoldItems = current.totalSoldItems + added.totalSoldItems
            )
        }
    }

    companion object {
        lateinit var plugin: SuspendingJavaPlugin
    }
}
