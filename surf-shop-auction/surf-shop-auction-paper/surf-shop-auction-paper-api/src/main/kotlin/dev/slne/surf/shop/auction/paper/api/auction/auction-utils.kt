package dev.slne.surf.shop.auction.paper.api.auction

import dev.slne.surf.shop.auction.api.auction.Auction
import dev.slne.surf.shop.auction.api.auction.AuctionManager
import org.bukkit.inventory.ItemStack
import java.time.OffsetDateTime
import java.util.*

val Auction.itemStack
    get() = run {
        val base64 = Base64.getDecoder().decode(itemData)

        ItemStack.deserializeBytes(base64)
    }

suspend fun AuctionManager.createAuction(
    ownerUuid: UUID,
    item: ItemStack,
    startingBid: Int,
    instantBuyEnabled: Boolean,
    instantBuyPrice: Int?,
    startsAt: OffsetDateTime,
    endsAt: OffsetDateTime,
): Auction = createAuction(
    ownerUuid = ownerUuid,
    itemData = Base64.getEncoder().encodeToString(item.serializeAsBytes()),
    startingBid = startingBid,
    instantBuyEnabled = instantBuyEnabled,
    instantBuyPrice = instantBuyPrice,
    startsAt = startsAt,
    endsAt = endsAt,
)