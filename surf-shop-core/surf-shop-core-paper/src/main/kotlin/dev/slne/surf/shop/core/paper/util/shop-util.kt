package dev.slne.surf.shop.core.paper.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import org.bukkit.Bukkit
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import java.util.*
import kotlin.time.Duration.Companion.hours

val Shop.dealCount get() = DealService.loadedDeals.count { it.shopInternalId == this.internalId }
val Shop.totalSoldItems
    get() = DealService.loadedDeals
        .filter { it.shopInternalId == this.internalId }
        .sumOf { it.amount }
val Shop.updatedShop
    get() = ShopService.loadedShops.firstOrNull { it.internalId == this.internalId }


private val nameCache = Caffeine.newBuilder().expireAfterWrite(12.hours).build<UUID, String> {
    Bukkit.getOfflinePlayer(it).name ?: "#Unbekannt"
}

val Shop.sellerName get() = nameCache.get(seller)
private val itemCache = mutableMapOf<Shop, ItemStack>()
val Shop.item: ItemStack
    get() = itemCache.getOrPut(this) {
        itemStackFromString(itemString)
    }

val Deal.boughtByName get() = nameCache.get(boughtBy)

private fun MutableSet<String>.addPotionTokens(itemStack: ItemStack) {
    val meta = itemStack.itemMeta as? PotionMeta ?: return

    meta.basePotionType?.let {
        add(it.key.key.lowercase())
    }
    meta.customEffects.forEach { effect ->
        add(effect.type.key.key.lowercase())
    }
}

fun Shop.rebuildSearchTokens() {
    searchableTokens = buildSet {
        add(item.type.name.lowercase())

        // Add potion effect tokens
        addPotionTokens(item)

        item.enchantments.keys.forEach {
            add(it.key.toString().lowercase())
        }

        val meta = item.itemMeta ?: return@buildSet

        if (meta is EnchantmentStorageMeta) {
            meta.storedEnchants.keys.forEach {
                add(it.key.toString().lowercase())
            }
        }

        if (meta is BlockStateMeta) {
            val blockState = meta.blockState

            if (blockState is ShulkerBox) {
                blockState.inventory.contents.filterNotNull().forEach {
                    if (it.type.isAir()) return@forEach
                    add(it.type.name.lowercase())
                    
                    // Add potion effect tokens for items in shulker box
                    addPotionTokens(it)

                    it.enchantments.keys.forEach { enchant ->
                        add(enchant.key.toString().lowercase())
                    }

                    val meta = it.itemMeta ?: return@forEach

                    if (meta is EnchantmentStorageMeta) {
                        meta.storedEnchants.keys.forEach { enchant ->
                            add(enchant.key.toString().lowercase())
                        }
                    }
                }
            }
        }
    }
}