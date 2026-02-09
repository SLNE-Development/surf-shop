package dev.slne.surf.shop.backend.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.backend.repository.dealRepository
import dev.slne.surf.shop.core.service.DealService
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Services
import org.bukkit.entity.Player
import java.util.*

@AutoService(DealService::class)
class DealServiceImpl : DealService, Services.Fallback {

    private val _deals = mutableObject2ObjectMapOf<UUID, Deal>()
    override val loadedDeals: ObjectSet<Deal> get() = _deals.values.toObjectSet()

    override suspend fun buyInternal(
        auction: Auction,
        amount: Int,
        buyer: UUID
    ): Deal {
        val boughtByRepository = dealRepository.buy(
            auction,
            amount,
            buyer,
            System.currentTimeMillis()
        )

        _deals[boughtByRepository.dealUuid] = boughtByRepository
        return boughtByRepository
    }

    override suspend fun buy(
        player: Player,
        auction: Auction,
        amount: Int
    ): Deal? {
        val updatedAuction =
            auctionService.loadedAuctions.firstOrNull { it.auctionUuid == auction.auctionUuid }
                ?: return null

        auctionService.blockAuctionAction(updatedAuction)

        if (auction.storedItemCount < amount) {
            buyInternal(auction, auction.storedItemCount, player.uniqueId)
            auctionService.saveAuction(updatedAuction.copy(storedItemCount = 0))

            player.sendText {
                appendSuccessPrefix()
                success("Du konntest nur ")
                variableValue("${auction.storedItemCount}x")
                success(" von ")
                variableValue("${amount}x ")
                append {
                    append(Component.translatable(auction.item.type.translationKey()))
                    hoverEvent(auction.item.asHoverEvent())
                }
                success("Items kaufen.")
            }
        } else {
            auctionService.saveAuction(updatedAuction.copy(storedItemCount = updatedAuction.storedItemCount - amount))

            player.sendText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("${amount}x ")
                append {
                    append(Component.translatable(auction.item.type.translationKey()))
                    hoverEvent(auction.item.asHoverEvent())
                }

                success("  gekauft.")
            }
        }

        return buyInternal(auction, amount, player.uniqueId)
    }

    override suspend fun fetchDeals() {

    }
}