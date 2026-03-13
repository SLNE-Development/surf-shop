package dev.slne.surf.shop.core.service

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.entity.Player
import java.util.*

val dealService = requiredService<DealService>()

interface DealService {
    val loadedDeals: ObjectSet<Deal>

    fun create(plugin: SuspendingJavaPlugin)

    suspend fun buyInternal(shop: Shop, amount: Int, buyer: UUID): Deal
    suspend fun buy(player: Player, shop: Shop, amount: Int): Deal.DealResult

    suspend fun fetchDeals()
}