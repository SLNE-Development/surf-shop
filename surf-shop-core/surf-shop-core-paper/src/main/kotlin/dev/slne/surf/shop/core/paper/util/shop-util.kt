package dev.slne.surf.shop.core.paper.util

import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

val Shop.dealCount get() = DealService.loadedDeals.count { it.shopInternalId == this.internalId }
val Shop.totalSoldItems
    get() = DealService.loadedDeals
        .filter { it.shopInternalId == this.internalId }
        .sumOf { it.amount }
val Shop.updatedShop
    get() = ShopService.loadedShops.firstOrNull { it.internalId == this.internalId }

val Shop.sellerName get() = Bukkit.getOfflinePlayer(seller).name ?: "#Unbekannt"
private val itemCache = mutableMapOf<Shop, ItemStack>()
val Shop.item: ItemStack
    get() = itemCache.getOrPut(this) {
        itemStackFromString(itemString)
    }

val Deal.boughtByName get() = Bukkit.getOfflinePlayer(boughtBy).name

fun Shop.rebuildSearchTokens() {
    searchableTokens = buildSet {
        add(item.type.name.lowercase())

        item.enchantments.keys.forEach {
            add(it.key.toString().lowercase())
        }

        val meta = item.itemMeta ?: return@buildSet

        if (meta is EnchantmentStorageMeta) {
            meta.storedEnchants.keys.forEach {
                add(it.key.toString().lowercase())
            }
        }
    }
}