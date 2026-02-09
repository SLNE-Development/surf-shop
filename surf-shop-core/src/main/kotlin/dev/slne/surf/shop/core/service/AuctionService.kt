package dev.slne.surf.shop.core.service

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.inventory.ItemStack
import java.util.*

val auctionService = requiredService<AuctionService>()

interface AuctionService {
    val loadedAuctions: ObjectSet<Auction>

    suspend fun createAuction(
        item: ItemStack,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Auction

    fun blockAuctionAction(auction: Auction)
    fun unblockAuction(auction: Auction)

    suspend fun saveAuction(auction: Auction): Auction
    suspend fun deleteAuction(auction: Auction): Boolean
    suspend fun fetchAuctions()
}