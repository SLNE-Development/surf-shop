package dev.slne.surf.shop.backend.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.backend.repository.dealRepository
import dev.slne.surf.shop.core.service.DealService
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.core.util.logger
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
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
    ): Deal? {
        val updatedShop =
            shopService.loadedShops.firstOrNull { it.shopUuid == shop.shopUuid }
                ?: return null

        shopService.blockShop(updatedShop)

        if (shop.storedItemCount < amount) {
            buyInternal(shop, shop.storedItemCount, player.uniqueId)
            shopService.saveShop(updatedShop.copy(storedItemCount = 0))

            player.sendText {
                appendSuccessPrefix()
                success("Du konntest nur ")
                variableValue("${shop.storedItemCount}x")
                success(" von ")
                variableValue("${amount}x ")
                append {
                    append(Component.translatable(shop.item.type.translationKey()))
                    hoverEvent(shop.item.asHoverEvent())
                }
                success("Items kaufen.")
            }
        } else {
            shopService.saveShop(updatedShop.copy(storedItemCount = updatedShop.storedItemCount - amount))

            player.sendText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("${amount}x ")
                append {
                    append(Component.translatable(shop.item.type.translationKey()))
                    hoverEvent(shop.item.asHoverEvent())
                }

                success("  gekauft.")
            }
        }

        return buyInternal(shop, amount, player.uniqueId)
    }

    override suspend fun fetchDeals() {
        logger.info("Fetching deals from database... (this may take a while!)")

        val loadedDeals = dealRepository.loadDeals()

        _deals.clear()
        loadedDeals.forEach { _deals[it.dealUuid] = it }

        logger.info("Loaded ${_deals.size} deals")
    }
}