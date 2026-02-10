package dev.slne.surf.shop.api.auction

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.*

data class Auction(
    val internalId: ULong,
    val auctionUuid: UUID,
    val item: ItemStack,
    val storedItemCount: Int,
    val pricePerItem: Int,
    val seller: UUID,
    val createdAt: Long,
) {
    val sellerName get() = Bukkit.getOfflinePlayer(seller).name
    fun isEmpty() = storedItemCount <= 0
}