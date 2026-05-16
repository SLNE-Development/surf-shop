package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object ShopsTable : ULongIdTable("shop_shops") {
    val shopUuid = nativeUuid("shop_uuid").uniqueIndex()
    val item = largeText("item_stack")
    val storedItemCount = integer("item_count")
    val pricePerItem = double("price_per_item")
    val seller = nativeUuid("seller")
    val createdAt = offsetDateTime("created_at")
}