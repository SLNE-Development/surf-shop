package dev.slne.surf.shop.auction.api.auction

import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable

private val manager = requiredService<AuctionManager>()

interface AuctionManager {
    val auctions: @Unmodifiable ObjectList<Auction>

    companion object : AuctionManager by manager {
        val INSTANCE get() = manager
    }
}