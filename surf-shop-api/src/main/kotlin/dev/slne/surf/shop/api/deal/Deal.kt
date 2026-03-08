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

    sealed class DealResult {
        data class Success(val deal: Deal) : DealResult()
        object ShopBlocked : DealResult()
        object ShopDeleted : DealResult()
        object InsufficientStock : DealResult()
        object SelfInsufficientFounds : DealResult()
        object OtherInsufficientFounds : DealResult()
        object TransactionFailed : DealResult()
    }
}
