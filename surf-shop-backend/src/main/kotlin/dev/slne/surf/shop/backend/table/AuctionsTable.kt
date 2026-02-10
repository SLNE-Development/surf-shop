package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.bukkit.inventory.ItemStack
import kotlin.io.encoding.Base64

object AuctionsTable : ULongIdTable("shop_auctions_v2") {
    val auctionUuid = nativeUuid("auction_uuid").uniqueIndex()
    val item = largeText("item_stack").transform(
        { itemStackFromString(it) },
        { itemStackToString(it) })
    val storedItemCount = integer("item_count")
    val pricePerItem = integer("price_per_item")
    val seller = nativeUuid("seller")
    val createdAt = long("created_at")
}

fun itemStackToString(itemStack: ItemStack): String = Base64.encode(itemStack.serializeAsBytes())
fun itemStackFromString(encoded: String): ItemStack =
    ItemStack.deserializeBytes(Base64.decode(encoded))