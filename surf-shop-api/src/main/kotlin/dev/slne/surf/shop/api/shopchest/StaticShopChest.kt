package dev.slne.surf.shop.api.shopchest

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import kotlinx.serialization.Serializable

@Serializable
data class StaticShopChest(
    val internalId: ULong,
    val chestUuid: SerializableUUID,
    val shopUuid: SerializableUUID?,
    val placedBy: SerializableUUID,
    val worldName: String,
    val x: Int,
    val y: Int,
    val z: Int,
)
