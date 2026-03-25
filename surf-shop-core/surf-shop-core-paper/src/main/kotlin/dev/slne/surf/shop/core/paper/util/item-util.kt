package dev.slne.surf.shop.core.paper.util

import org.bukkit.inventory.ItemStack
import kotlin.io.encoding.Base64

fun itemStackToString(itemStack: ItemStack): String = Base64.encode(itemStack.serializeAsBytes())
fun itemStackFromString(encoded: String): ItemStack =
    ItemStack.deserializeBytes(Base64.decode(encoded))