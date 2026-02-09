package dev.slne.surf.shop.backend.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.backend.table.DealsTable
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import java.util.*

val dealRepository = DealRepository()

class DealRepository {
    suspend fun loadDeals(): ObjectSet<Deal> = suspendTransaction {
        DealsTable.selectAll().map {
            createDeal(it)
        }.toSet().toObjectSet()
    }

    suspend fun buy(
        auction: Auction,
        amount: Int,
        buyer: UUID,
        boughtAt: Long
    ): Deal = suspendTransaction {
        DealsTable.insertReturning {
            it[this.dealUuid] = UUID.randomUUID()
            it[this.auctionInternalId] = auction.internalId
            it[this.amount] = amount
            it[this.boughtBy] = buyer
            it[this.boughtAt] = boughtAt
        }.map {
            createDeal(it)
        }.firstOrNull() ?: error("Failed to create deal")
    }

    private fun createDeal(row: ResultRow) = Deal(
        dealInternalId = row[DealsTable.id].value,
        dealUuid = row[DealsTable.dealUuid],
        auctionInternalId = row[DealsTable.auctionInternalId],
        amount = row[DealsTable.amount],
        boughtBy = row[DealsTable.boughtBy],
        boughtAt = row[DealsTable.boughtAt]
    )
}