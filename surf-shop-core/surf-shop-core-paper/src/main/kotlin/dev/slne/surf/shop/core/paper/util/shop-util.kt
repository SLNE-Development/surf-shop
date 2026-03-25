package dev.slne.surf.shop.core.paper.util

import dev.slne.surf.shop.api.shop.Shop
import org.bukkit.Bukkit

val Shop.sellerName get() = Bukkit.getOfflinePlayer(seller).name ?: "#Unbekannt"
val Shop.item get() =