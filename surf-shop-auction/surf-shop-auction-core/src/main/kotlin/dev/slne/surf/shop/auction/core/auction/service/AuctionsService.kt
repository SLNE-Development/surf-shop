package dev.slne.surf.shop.auction.core.auction.service

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.shop.auction.api.auction.Auction
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable

interface AuctionsService {
    val auctions: @Unmodifiable ObjectList<Auction>
    suspend fun cacheAuctions()
    suspend fun findAuctions(): ObjectList<Auction>

    suspend fun placeBid(
        auction: Auction,
        bidder: SurfPlayer,
        amount: Int
    )

    suspend fun instantBuy(
        auction: Auction,
        buyer: SurfPlayer
    )

    companion object : AuctionsService by AuctionsServiceImpl
}