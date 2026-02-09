package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object AuctionsTable : ULongIdTable("shop_auctions") {
    val auctionUuid = nativeUuid("auction_uuid").uniqueIndex()
    val item = binary("item")
    val storedItemCount = integer("item_count")
    val pricePerItem = integer("price_per_item")
    val seller = nativeUuid("seller")
    val createdAt = long("created_at")
}