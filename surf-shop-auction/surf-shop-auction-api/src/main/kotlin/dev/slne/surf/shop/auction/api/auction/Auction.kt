package dev.slne.surf.shop.auction.api.auction

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.shop.auction.api.auction.bid.AuctionBid
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import java.time.OffsetDateTime
import java.util.*

interface Auction {
    val uuid: UUID
    suspend fun owner(): SurfPlayer

    val itemData: String

    val bids: @Unmodifiable ObjectList<AuctionBid>
    val startingBid: Int
    val currentBid: AuctionBid?

    val instantBuyEnabled: Boolean
    val instantBuyPrice: Int?

    val startsAt: OffsetDateTime
    val endsAt: OffsetDateTime

    val serverName: String
    val server: SurfServer?

    suspend fun placeBid(bidder: SurfPlayer, amount: Int)
    suspend fun instantBuy(buyer: SurfPlayer)
}