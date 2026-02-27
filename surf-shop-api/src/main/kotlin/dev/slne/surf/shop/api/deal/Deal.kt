package dev.slne.surf.shop.api.deal

import org.bukkit.Bukkit
import java.time.OffsetDateTime
import java.util.*

data class Deal(
    val dealInternalId: ULong,
    val dealUuid: UUID,
    val shopInternalId: ULong,
    val amount: Int,
    val boughtBy: UUID,
    val boughtAt: OffsetDateTime
) {
    val boughtByName get() = Bukkit.getOfflinePlayer(boughtBy).name
}
