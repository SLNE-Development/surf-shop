package dev.slne.surf.shop.api.shop

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.OffsetDateTime
import java.util.*

@Serializable
data class Shop(
    val internalId: ULong,
    val shopUuid: SerializableUUID,
    val itemString: String,
    val storedItemCount: Int,
    val pricePerItem: Double,
    val seller: SerializableUUID,
    val createdAt: SerializableOffsetDateTime,
) {
    var isBlocked: Boolean = false

    fun isEmpty() = storedItemCount <= 0

    @Transient
    lateinit var searchableTokens: Set<String>

    companion object {
        fun empty() = Shop(
            internalId = 0uL,
            shopUuid = UUID.randomUUID(),
            itemString = "",
            storedItemCount = 0,
            pricePerItem = 0.0,
            seller = UUID.randomUUID(),
            createdAt = OffsetDateTime.MIN
        )
    }
}