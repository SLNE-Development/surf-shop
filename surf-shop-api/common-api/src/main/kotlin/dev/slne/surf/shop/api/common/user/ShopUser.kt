package dev.slne.surf.shop.api.common.user

import net.kyori.adventure.text.ComponentLike
import java.util.*

interface ShopUser : ComponentLike {

    val uuid: UUID

}