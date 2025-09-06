package dev.slne.surf.shop.core.common.impl

import dev.slne.surf.shop.api.common.Shop
import dev.slne.surf.shop.api.common.ShopCategory
import dev.slne.surf.shop.api.common.user.ShopUser
import net.kyori.adventure.text.Component

class ShopImpl(
    override val category: ShopCategory,
    override val owner: ShopUser,
    override val serverName: String,
    override val itemStackData: ByteArray,
    override val pricePerItem: Int,
) : Shop {

    override fun asComponent(): Component {
        TODO("Not yet implemented")
    }
}