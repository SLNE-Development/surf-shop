package dev.slne.surf.shop.core.common.service

import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

val dealService = requiredService<DealService>()

interface DealService {
    val loadedDeals: ObjectSet<Deal>

    suspend fun buyInternal(shop: Shop, amount: Int, buyer: UUID): Deal
    suspend fun buy(playerUuid: UUID, shop: Shop, amount: Int): Deal.DealResult

    suspend fun fetchDeals()
}