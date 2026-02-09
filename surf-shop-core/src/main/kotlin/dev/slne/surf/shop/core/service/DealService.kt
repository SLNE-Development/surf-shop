package dev.slne.surf.shop.core.service

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.UUID

val dealService = requiredService<DealService>()

interface DealService {
    val loadedDeals: ObjectSet<Deal>
    suspend fun buy(auction: Auction, amount: Int, buyer: UUID): Deal?

    suspend fun fetchDeals()
}