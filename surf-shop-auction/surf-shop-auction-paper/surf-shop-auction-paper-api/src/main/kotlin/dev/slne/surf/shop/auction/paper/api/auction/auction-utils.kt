package dev.slne.surf.shop.auction.paper.api.auction

import dev.slne.surf.shop.auction.api.auction.Auction
import org.bukkit.inventory.ItemStack
import java.util.*

val Auction.itemStack
    get() = run {
        val base64 = Base64.getDecoder().decode(itemData)

        ItemStack.deserializeBytes(base64)
    }