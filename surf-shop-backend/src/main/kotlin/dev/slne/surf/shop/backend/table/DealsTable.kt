package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object DealsTable : ULongIdTable("auction_deals") {
    val dealUuid = nativeUuid("deal_uuid").uniqueIndex()
    val auctionInternalId = ulong("auction_internal_id").references(AuctionsTable.id)
    val amount = integer("amount")
    val boughtBy = nativeUuid("bought_by")
    val boughtAt = long("bought_at")
}