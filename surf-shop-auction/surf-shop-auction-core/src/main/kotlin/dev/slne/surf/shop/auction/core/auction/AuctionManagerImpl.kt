package dev.slne.surf.shop.auction.core.auction

import com.google.auto.service.AutoService
import dev.slne.surf.shop.auction.api.auction.Auction
import dev.slne.surf.shop.auction.api.auction.AuctionManager
import dev.slne.surf.shop.auction.core.auction.service.AuctionService
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services
import org.jetbrains.annotations.Unmodifiable
import java.time.OffsetDateTime
import java.util.*

@AutoService(AuctionManager::class)
class AuctionManagerImpl : AuctionManager, Services.Fallback {
    override val auctions: @Unmodifiable ObjectList<Auction>
        get() = AuctionService.auctions

    override suspend fun createAuction(
        ownerUuid: UUID,
        itemData: String,
        startingBid: Int,
        instantBuyEnabled: Boolean,
        instantBuyPrice: Int?,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime
    ) = AuctionService.createAuction(
        ownerUuid = ownerUuid,
        itemData = itemData,
        startingBid = startingBid,
        instantBuyEnabled = instantBuyEnabled,
        instantBuyPrice = instantBuyPrice,
        startsAt = startsAt,
        endsAt = endsAt
    )
}