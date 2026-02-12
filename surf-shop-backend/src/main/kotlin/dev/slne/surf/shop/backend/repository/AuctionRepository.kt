package dev.slne.surf.shop.backend.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.backend.table.AuctionsTable
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import org.bukkit.inventory.ItemStack
import java.time.OffsetDateTime
import java.util.*

val auctionRepository = AuctionRepository()

class AuctionRepository {
    suspend fun loadAuctions(): ObjectSet<Auction> = suspendTransaction {
        AuctionsTable.selectAll().map {
            createAuction(it)
        }.toSet().toObjectSet()
    }

    suspend fun createAuction(
        item: ItemStack,
        storedItemCount: Int,
        pricePerItem: Int,
        seller: UUID,
        createdAt: OffsetDateTime
    ): Auction = suspendTransaction {
        AuctionsTable.insertReturning {
            it[this.auctionUuid] = UUID.randomUUID()
            it[this.item] = item
            it[this.storedItemCount] = storedItemCount
            it[this.pricePerItem] = pricePerItem
            it[this.seller] = seller
            it[this.createdAt] = createdAt
        }.map {
            createAuction(it)
        }.firstOrNull() ?: error("Failed to create auction")
    }

    suspend fun saveAuction(auction: Auction) = suspendTransaction {
        AuctionsTable.upsert {
            it[auctionUuid] = auction.auctionUuid
            it[item] = auction.item
            it[storedItemCount] = auction.storedItemCount
            it[pricePerItem] = auction.pricePerItem
            it[seller] = auction.seller
            it[createdAt] = auction.createdAt
        }
    }

    suspend fun deleteAuction(auction: Auction) = suspendTransaction {
        AuctionsTable.deleteWhere { AuctionsTable.auctionUuid eq auction.auctionUuid } > 0
    }

    private fun createAuction(row: ResultRow) = Auction(
        internalId = row[AuctionsTable.id].value,
        auctionUuid = row[AuctionsTable.auctionUuid],
        item = row[AuctionsTable.item],
        storedItemCount = row[AuctionsTable.storedItemCount],
        pricePerItem = row[AuctionsTable.pricePerItem],
        seller = row[AuctionsTable.seller],
        createdAt = row[AuctionsTable.createdAt]
    )
}