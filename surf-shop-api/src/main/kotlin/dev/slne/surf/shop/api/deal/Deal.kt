package dev.slne.surf.shop.api.deal

import dev.slne.surf.surfapi.bukkit.api.SurfBukkitApi
import org.bukkit.Bukkit
import java.util.UUID

data class Deal(
    val dealInternalId: ULong,
    val dealUuid: UUID,
    val auctionInternalId: ULong,
    val amount: Int,
    val boughtBy: UUID,
    val boughtAt: Long
) {
    val boughtByName get() = Bukkit.getOfflinePlayer(boughtBy).name
}
