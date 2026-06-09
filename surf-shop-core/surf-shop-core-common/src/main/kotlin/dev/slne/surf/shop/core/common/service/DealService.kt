package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import java.util.*

private val service = requiredService<DealService>()

data class ShopDealStats(
    val dealCount: Int = 0,
    val totalSoldItems: Int = 0
)

interface DealService {
    val loadedDeals: Collection<Deal>

    fun loadedDealsSnapshot(): List<Deal> = loadedDeals.toList()
    fun getDealStats(shopInternalId: ULong): ShopDealStats

    suspend fun buyInternal(shop: Shop, amount: Int, buyer: UUID): Deal
    suspend fun buy(playerUuid: UUID, shop: Shop, amount: Int): Deal.DealResult

    suspend fun fetchDeals()

    companion object : DealService by service
}
