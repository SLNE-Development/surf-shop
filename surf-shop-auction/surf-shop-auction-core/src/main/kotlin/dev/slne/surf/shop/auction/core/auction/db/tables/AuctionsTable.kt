package dev.slne.surf.shop.auction.core.auction.db.tables

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.table.AuditableLongIdTable

object AuctionsTable : AuditableLongIdTable("shop_auctions") {
    val uuid = nativeUuid("auction_uuid")
    val ownerUuid = nativeUuid("owner_uuid")

    val itemData = largeText("item_data")

    val startingBid = integer("starting_bid")
    val instantBuyEnabled = bool("instant_buy_enabled").default(false)
    val instantBuyPrice = integer("instant_buy_price").nullable()

    val startsAt = offsetDateTime("starts_at")
    val endsAt = offsetDateTime("ends_at")

    val serverName = varchar("server_name", 255)
}