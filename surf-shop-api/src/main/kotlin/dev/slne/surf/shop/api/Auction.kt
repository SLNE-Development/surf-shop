package dev.slne.surf.shop.api

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class Auction(
    val internalId: ULong,
    val auctionUuid: UUID,
    val item: ItemStack,
    val itemCount: Int,
    val pricePerItem: Int,
    val seller: UUID,
    val createdAt: Long,
    val boughtBuy: UUID?
) {
    val isBought: Boolean
        get() = boughtBuy != null

    val sellerName get() = Bukkit.getOfflinePlayer(seller).name
    val buyerName get() = boughtBuy?.let { Bukkit.getOfflinePlayer(it).name }
}
