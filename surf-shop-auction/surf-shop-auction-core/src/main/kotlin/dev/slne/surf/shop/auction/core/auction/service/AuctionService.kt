package dev.slne.surf.shop.auction.core.auction.service

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.shop.auction.api.auction.Auction
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import java.time.OffsetDateTime
import java.util.*

interface AuctionService {
    val auctions: @Unmodifiable ObjectList<Auction>

    suspend fun cacheAuctions()
    fun cacheAuction(auction: Auction)

    suspend fun findAuctions(): ObjectList<Auction>
    
    suspend fun createAuction(
        ownerUuid: UUID,
        itemData: String,
        startingBid: Int,
        instantBuyEnabled: Boolean,
        instantBuyPrice: Int?,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime,
    ): Auction

    suspend fun placeBid(
        auction: Auction,
        bidder: SurfPlayer,
        amount: Int
    )

    suspend fun instantBuy(
        auction: Auction,
        buyer: SurfPlayer
    )

    companion object : AuctionService by AuctionServiceImpl
}