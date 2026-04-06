package dev.slne.surf.shop.core.common.service

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

private val service = requiredService<StaticShopChestService>()

interface StaticShopChestService {
    val loadedChests: ObjectSet<StaticShopChest>

    suspend fun createChest(
        shopUuid: UUID?,
        placedBy: UUID,
        worldName: String,
        x: Int,
        y: Int,
        z: Int
    ): StaticShopChest

    suspend fun updateChestShop(chestUuid: UUID, shopUuid: UUID?): StaticShopChest?
    suspend fun deleteChest(chestUuid: UUID): Boolean
    suspend fun fetchChests()

    fun getChestAt(worldName: String, x: Int, y: Int, z: Int): StaticShopChest?

    companion object : StaticShopChestService by service
}
