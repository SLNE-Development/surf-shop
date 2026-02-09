package dev.slne.surf.shop.core.service

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.entity.Player
import java.util.*

val dealService = requiredService<DealService>()

interface DealService {
    val loadedDeals: ObjectSet<Deal>
    suspend fun buyInternal(auction: Auction, amount: Int, buyer: UUID): Deal
    suspend fun buy(player: Player, auction: Auction, amount: Int): Deal?

    suspend fun fetchDeals()
}