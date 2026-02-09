package dev.slne.surf.shop.backend.service

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.AuctionService
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services
import org.bukkit.inventory.ItemStack
import java.util.*

@AutoService(AuctionService::class)
class AuctionServiceImpl : AuctionService, Services.Fallback {
    private val _auctions = mutableObject2ObjectMapOf<UUID, Auction>()
    override val loadedAuctions: ObjectSet<Auction> get() = _auctions.values.toObjectSet()

    override suspend fun createAuction(
        item: ItemStack,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID
    ): Auction {

    }

    override suspend fun saveAuction(auction: Auction): Auction {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAuction(auction: Auction): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun fetchAuctions() {
        TODO("Not yet implemented")
    }
}