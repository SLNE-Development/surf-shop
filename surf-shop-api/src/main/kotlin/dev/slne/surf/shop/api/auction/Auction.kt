package dev.slne.surf.shop.api.auction

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import java.util.*

data class Auction(
    val internalId: ULong,
    val auctionUuid: UUID,
    val item: ItemStack,
    val storedItemCount: Int,
    val pricePerItem: Int,
    val seller: UUID,
    val createdAt: Long, // TODO: Change to OffsetDateTime!  ~ red
) {
    var isBlocked: Boolean = false

    val sellerName get() = Bukkit.getOfflinePlayer(seller).name ?: "#Unbekannt"
    fun isEmpty() = storedItemCount <= 0

    lateinit var searchableTokens: Set<String>

    fun rebuildSearchTokens() {
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
}