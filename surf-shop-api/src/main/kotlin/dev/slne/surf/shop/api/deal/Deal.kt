package dev.slne.surf.shop.api.deal

import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class Deal(
    val dealInternalId: ULong,
    val dealUuid: SerializableUUID,
    val shopInternalId: ULong,
    val amount: Int,
    val boughtBy: SerializableUUID,
    val boughtAt: SerializableOffsetDateTime
) {
    sealed class DealResult {
        data class Success(val deal: Deal) : DealResult()
        object ShopBlocked : DealResult()
        object ShopDeleted : DealResult()
        object InsufficientStock : DealResult()
        object SelfInsufficientFounds : DealResult()
        object OtherInsufficientFounds : DealResult()
        object TransactionFailed : DealResult()
        object PlayerNotFound : DealResult()
    }
}
