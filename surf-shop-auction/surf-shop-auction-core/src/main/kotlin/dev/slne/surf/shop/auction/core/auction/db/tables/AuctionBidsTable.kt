package dev.slne.surf.shop.auction.core.auction.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

object AuctionBidsTable : AuditableLongIdTable("shop_auction_bids") {
    val auction = reference(
        "auction_id",
        AuctionsTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val bidderUuid = nativeUuid("bidder_uuid")
    val amount = integer("amount")
}