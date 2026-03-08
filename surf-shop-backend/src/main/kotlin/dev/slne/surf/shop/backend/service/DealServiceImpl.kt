package dev.slne.surf.shop.backend.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.backend.repository.dealRepository
import dev.slne.surf.shop.core.service.DealService
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.core.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import org.bukkit.entity.Player
import java.time.OffsetDateTime
import java.util.*

@AutoService(DealService::class)
class DealServiceImpl : DealService, Services.Fallback {

    private val _deals = mutableObject2ObjectMapOf<UUID, Deal>()
    override val loadedDeals: ObjectSet<Deal> get() = _deals.values.toObjectSet()

    override suspend fun buyInternal(
        shop: Shop,
        amount: Int,
        buyer: UUID
    ): Deal {
        val boughtByRepository = dealRepository.buy(
            shop,
            amount,
            buyer,
            OffsetDateTime.now()
        )

        _deals[boughtByRepository.dealUuid] = boughtByRepository
        return boughtByRepository
    }

    override suspend fun buy(
        player: Player,
        shop: Shop,
        amount: Int
    ): Deal.DealResult {
        val actualShop =
            shopService.loadedShops.firstOrNull { it.shopUuid == shop.shopUuid }
                ?: return Deal.DealResult.ShopDeleted

        val storedAmount = actualShop.storedItemCount

        if (storedAmount == 0) {
            return Deal.DealResult.InsufficientStock
        }

        return if (storedAmount < amount) {
            buy0(player, actualShop, storedAmount)
        } else {
            buy0(player, actualShop, amount)
        }
    }

    private suspend fun buy0(player: Player, shop: Shop, amount: Int): Deal.DealResult {
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
                shopService.saveShop(shop.copy(storedItemCount = shop.storedItemCount - amount))

                val boughtByRepository = dealRepository.buy(
                    shop,
                    amount,
                    player.uniqueId,
                    OffsetDateTime.now()
                )

                _deals[boughtByRepository.dealUuid] = boughtByRepository

                return Deal.DealResult.Success(boughtByRepository)
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
}