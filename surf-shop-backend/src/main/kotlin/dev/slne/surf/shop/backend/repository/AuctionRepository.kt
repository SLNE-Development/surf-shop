package dev.slne.surf.shop.backend.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.backend.table.AuctionsTable
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.flow.map

val auctionRepository = AuctionRepository()

class AuctionRepository {
    suspend fun loadAuctions(): ObjectSet<Auction> = suspendTransaction {
        AuctionsTable.selectAll().map {

        }
    }

    fun createAuction(row: ResultRow) = Auction(
        internalId = row[AuctionsTable.id].value,
        auctionUuid = row[AuctionsTable.auctionUuid],
        item = row[AuctionsTable.item],
        storedItemCount = row[AuctionsTable.storedItemCount],
        pricePerItem = row[AuctionsTable.pricePerItem],
        seller = row[AuctionsTable.seller],
        createdAt = row[AuctionsTable.createdAt]
    )
}