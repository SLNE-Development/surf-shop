package dev.slne.surf.shop.core.paper.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.api.paper.util.namespacedKey
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.search.SearchTranslations
import dev.slne.surf.shop.core.common.service.ShopService
import net.kyori.adventure.translation.Translatable
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

private val BLACKLISTED_ITEMS = objectListOf(
    Material.PLAYER_HEAD
)

val Shop.updatedShop
    get() = ShopService.getShopByInternalId(this.internalId)


private val nameCache = Caffeine.newBuilder().expireAfterWrite(12.hours).build<UUID, String> {
    Bukkit.getOfflinePlayer(it).name ?: "#Unbekannt"
}

val Shop.sellerName get() = nameCache.get(seller)
private val itemCache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterAccess(6, TimeUnit.HOURS)
    .build<String, ItemStack>()

val Shop.item: ItemStack
    get() = itemCache.get(itemString) { itemStackFromString(it) }

val Deal.boughtByName get() = nameCache.get(boughtBy)

/** Adds the names of [translatable] in every loaded search language (see [SearchTranslations]). */
private fun MutableSet<String>.addTranslatedNames(translatable: Translatable) {
    addAll(SearchTranslations.current.namesOf(translatable))
}

private fun MutableSet<String>.addPotionTokens(itemStack: ItemStack) {
    val meta = itemStack.itemMeta as? PotionMeta ?: return

    meta.basePotionType?.let {
        add(it.key.key.lowercase().replace(" ", "_"))
        it.potionEffects.forEach { effect -> addTranslatedNames(effect.type) }
    }
    meta.customEffects.forEach { effect ->
        add(effect.type.key.key.lowercase().replace(" ", "_"))
        addTranslatedNames(effect.type)
    }
}

/** Keys and translated names of the item type, potion effects and (stored) enchantments of [itemStack]. */
private fun MutableSet<String>.addItemTokens(itemStack: ItemStack) {
    add(itemStack.type.name.lowercase())
    addTranslatedNames(itemStack.type)

    addPotionTokens(itemStack)

    itemStack.enchantments.keys.forEach {
        add(it.key.toString().lowercase())
        addTranslatedNames(it)
    }

    val meta = itemStack.itemMeta as? EnchantmentStorageMeta ?: return
    meta.storedEnchants.keys.forEach {
        add(it.key.toString().lowercase())
        addTranslatedNames(it)
    }
}

val denyShopKey = namespacedKey("deny_shop")

fun isAllowedToSell(item: ItemStack): Boolean {
    return !BLACKLISTED_ITEMS.contains(item.type) && !item.persistentDataContainer.has(denyShopKey)
}

fun Shop.rebuildSearchTokens() {
    searchableTokens = buildSet {
        addItemTokens(item)

        val blockState = (item.itemMeta as? BlockStateMeta)?.blockState
        if (blockState is ShulkerBox) {
            blockState.inventory.contents.filterNotNull().forEach {
                if (!it.type.isAir()) addItemTokens(it)
            }
        }
    }
}
