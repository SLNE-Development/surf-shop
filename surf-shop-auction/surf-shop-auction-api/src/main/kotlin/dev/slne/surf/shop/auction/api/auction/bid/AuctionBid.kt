package dev.slne.surf.shop.auction.api.auction.bid

import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class AuctionBid(
    val bidderUuid: SerializableUUID,
    val amount: Int,
    val timestamp: SerializableOffsetDateTime
)
