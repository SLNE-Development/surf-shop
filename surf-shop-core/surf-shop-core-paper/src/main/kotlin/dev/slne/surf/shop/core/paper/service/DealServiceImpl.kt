package dev.slne.surf.shop.core.paper.service

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@AutoService(DealService::class)
class DealServiceImpl : DealService, Services.Fallback {

    private val _deals = mutableObject2ObjectMapOf<UUID, Deal>()
    override val loadedDeals: ObjectSet<Deal> get() = _deals.values.toObjectSet()
    private lateinit var plugin: SuspendingJavaPlugin

    override fun create(plugin: SuspendingJavaPlugin) {
        this.plugin = plugin
    }

    private val shopLocks = ConcurrentHashMap<UUID, Mutex>()

    private fun getLock(shopUuid: UUID): Mutex {
        return shopLocks.computeIfAbsent(shopUuid) { Mutex() }
    }

    override suspend fun buyInternal(
        shop: Shop,
        amount: Int,
        buyer: UUID
    ): Deal {
        val bought = dealRepository.buy(
            shop,
            amount,
            buyer,
            OffsetDateTime.now()
        )

        _deals[bought.dealUuid] = bought
        return bought
    }

    override suspend fun buy(
        player: Player,
        shop: Shop,
        amount: Int
    ): Deal.DealResult {

        val actualShop =
            shopService.loadedShops.firstOrNull { it.shopUuid == shop.shopUuid }
                ?: return Deal.DealResult.ShopDeleted

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
            receiverAccount
        )

        when (transactionResult) {
            is TransactionResult.DatabaseError -> return Deal.DealResult.TransactionFailed
            is TransactionResult.ReceiverInsufficientFunds -> return Deal.DealResult.SelfInsufficientFounds
            is TransactionResult.SenderInsufficientFunds -> return Deal.DealResult.OtherInsufficientFounds
            else -> {
                val updatedShop = shop.copy(
                    storedItemCount = shop.storedItemCount - amount
                )

                shopService.saveShop(updatedShop)

                val deal = dealRepository.buy(
                    updatedShop,
                    amount,
                    player.uniqueId,
                    OffsetDateTime.now()
                )

                _deals[deal.dealUuid] = deal

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

                return Deal.DealResult.Success(deal)
            }
        }
    }

    override suspend fun fetchDeals() {
        logger.info("Fetching deals from database... (this may take a while!)")

        val loadedDeals = dealRepository.loadDeals()

        _deals.clear()
        loadedDeals.forEach { _deals[it.dealUuid] = it }

        logger.info("Loaded ${_deals.size} deals")
    }

    fun removeShopLock(shopUuid: UUID) {
        shopLocks.remove(shopUuid)
    }
}