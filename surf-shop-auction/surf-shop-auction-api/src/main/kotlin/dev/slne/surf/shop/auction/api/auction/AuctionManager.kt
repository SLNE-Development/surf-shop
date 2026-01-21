package dev.slne.surf.shop.auction.api.auction

import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import java.time.OffsetDateTime
import java.util.*

private val manager = requiredService<AuctionManager>()

interface AuctionManager {
    val auctions: @Unmodifiable ObjectList<Auction>

    suspend fun createAuction(
        ownerUuid: UUID,
        itemData: String,
        startingBid: Int,
        instantBuyEnabled: Boolean,
        instantBuyPrice: Int?,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime,
    ): Auction

    companion object : AuctionManager by manager {
        val INSTANCE get() = manager
    }
}