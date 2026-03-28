package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object StaticShopChestsTable : ULongIdTable("shop_static_shop_chests") {
    val chestUuid = nativeUuid("chest_uuid").uniqueIndex()
    val shopUuid = nativeUuid("shop_uuid").nullable()
    val placedBy = nativeUuid("placed_by")
    val worldName = varchar("world_name", 255)
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
}
