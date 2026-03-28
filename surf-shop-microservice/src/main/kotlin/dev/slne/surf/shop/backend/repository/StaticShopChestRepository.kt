package dev.slne.surf.shop.backend.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.backend.table.StaticShopChestsTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

val staticShopChestRepository = StaticShopChestRepository()

class StaticShopChestRepository {
    suspend fun loadAll(): List<StaticShopChest> = suspendTransaction {
        StaticShopChestsTable.selectAll().map {
            createStaticShopChest(it)
        }.toList()
    }

    suspend fun create(
        shopUuid: UUID?,
        placedBy: UUID,
        worldName: String,
        x: Int,
        y: Int,
        z: Int
    ): StaticShopChest = suspendTransaction {
        StaticShopChestsTable.insertReturning {
            it[this.chestUuid] = UUID.randomUUID()
            it[this.shopUuid] = shopUuid
            it[this.placedBy] = placedBy
            it[this.worldName] = worldName
            it[this.x] = x
            it[this.y] = y
            it[this.z] = z
        }.map {
            createStaticShopChest(it)
        }.firstOrNull() ?: error("Failed to create static shop chest")
    }

    suspend fun updateShopUuid(chestUuid: UUID, shopUuid: UUID?) = suspendTransaction {
        StaticShopChestsTable.update({ StaticShopChestsTable.chestUuid eq chestUuid }) {
            it[this.shopUuid] = shopUuid
        }
    }

    suspend fun delete(chestUuid: UUID) = suspendTransaction {
        StaticShopChestsTable.deleteWhere { StaticShopChestsTable.chestUuid eq chestUuid } > 0
    }

    private fun createStaticShopChest(row: ResultRow) = StaticShopChest(
        internalId = row[StaticShopChestsTable.id].value,
        chestUuid = row[StaticShopChestsTable.chestUuid],
        shopUuid = row[StaticShopChestsTable.shopUuid],
        placedBy = row[StaticShopChestsTable.placedBy],
        worldName = row[StaticShopChestsTable.worldName],
        x = row[StaticShopChestsTable.x],
        y = row[StaticShopChestsTable.y],
        z = row[StaticShopChestsTable.z]
    )
}
