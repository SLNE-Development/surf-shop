package dev.slne.surf.shop.backend.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object DealsTable : ULongIdTable("shop_deals") {
    val dealUuid = nativeUuid("deal_uuid").uniqueIndex()
    val shopInternalId =
        ulong("shop_internal_id").references(ShopsTable.id, ReferenceOption.CASCADE)
    val amount = integer("amount")
    val boughtBy = nativeUuid("bought_by")
    val boughtAt = offsetDateTime("bought_at")
}