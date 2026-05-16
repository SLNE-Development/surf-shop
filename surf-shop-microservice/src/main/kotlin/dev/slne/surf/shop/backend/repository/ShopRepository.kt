package dev.slne.surf.shop.backend.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.backend.table.ShopsTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime
import java.util.*

val shopRepository = ShopRepository()

class ShopRepository {
    suspend fun loadShops(): List<Shop> = suspendTransaction {
        ShopsTable.selectAll().map {
            createShop(it)
        }.toList()
    }

    suspend fun createShop(
        itemString: String,
        storedItemCount: Int,
        pricePerItem: Double,
        seller: UUID,
        createdAt: OffsetDateTime
    ): Shop = suspendTransaction {
        ShopsTable.insertReturning {
            it[this.shopUuid] = UUID.randomUUID()
            it[this.item] = itemString
            it[this.storedItemCount] = storedItemCount
            it[this.pricePerItem] = pricePerItem
            it[this.seller] = seller
            it[this.createdAt] = createdAt
        }.map {
            createShop(it)
        }.firstOrNull() ?: error("Failed to create shop")
    }

    suspend fun saveShop(shop: Shop) = suspendTransaction {
        ShopsTable.upsert {
            it[shopUuid] = shop.shopUuid
            it[item] = shop.itemString
            it[storedItemCount] = shop.storedItemCount
            it[pricePerItem] = shop.pricePerItem
            it[seller] = shop.seller
            it[createdAt] = shop.createdAt
        }
    }

    suspend fun deleteShop(shop: Shop) = suspendTransaction {
        ShopsTable.deleteWhere { ShopsTable.shopUuid eq shop.shopUuid } > 0
    }

    private fun createShop(row: ResultRow) = Shop(
        internalId = row[ShopsTable.id].value,
        shopUuid = row[ShopsTable.shopUuid],
        itemString = row[ShopsTable.item],
        storedItemCount = row[ShopsTable.storedItemCount],
        pricePerItem = row[ShopsTable.pricePerItem],
        seller = row[ShopsTable.seller],
        createdAt = row[ShopsTable.createdAt]
    )
}