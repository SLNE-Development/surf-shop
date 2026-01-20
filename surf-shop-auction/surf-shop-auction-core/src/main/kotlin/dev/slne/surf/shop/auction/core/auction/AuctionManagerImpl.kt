package dev.slne.surf.shop.auction.core.auction

import com.google.auto.service.AutoService
import dev.slne.surf.shop.auction.api.auction.Auction
import dev.slne.surf.shop.auction.api.auction.AuctionManager
import dev.slne.surf.shop.auction.core.auction.service.AuctionsService
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.util.Services
import org.jetbrains.annotations.Unmodifiable

@AutoService(AuctionManager::class)
class AuctionManagerImpl : AuctionManager, Services.Fallback {
    override val auctions: @Unmodifiable ObjectList<Auction>
        get() = AuctionsService.auctions
}