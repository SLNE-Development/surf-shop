package dev.slne.surf.shop.api.common

import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.shop.api.common.user.ShopUser
import net.kyori.adventure.text.ComponentLike

interface Shop : ComponentLike {

    val owner: ShopUser

    val serverName: String
    val server get() = CloudServerManager.retrieveServerByName(serverName)

    val itemStackData: ByteArray
    val category: ShopCategory
    val pricePerItem: Int
}