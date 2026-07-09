package dev.slne.surf.shop.paper.menu

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import me.devnatan.inventoryframework.View
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

val View.outlineItem: ItemStack
    get() = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

val View.loadingItem
    get() = buildItem(Material.YELLOW_STAINED_GLASS_PANE) {
        displayName {
            shopColored("Lädt...")
        }
    }

fun Player.playNoSound() = this.playSound(true) {
    type(Sound.ENTITY_VILLAGER_NO)
}

fun ItemStack.shulkerBoxState(): ShulkerBox? {
    if (!Tag.SHULKER_BOXES.isTagged(type)) return null
    val meta = itemMeta as? BlockStateMeta ?: return null
    return meta.blockState as? ShulkerBox
}

fun ItemStack.shulkerFreeCapacityFor(item: ItemStack): Int {
    val state = shulkerBoxState() ?: return 0
    val maxStack = item.maxStackSize

    return state.inventory.contents.sumOf { slot ->
        when {
            slot == null || slot.type.isAir -> maxStack
            slot.isSimilar(item) -> (maxStack - slot.amount).coerceAtLeast(0)
            else -> 0
        }
    }
}

fun Player.findFillableShulker(item: ItemStack): ItemStack? =
    inventory.storageContents.firstOrNull { it != null && it.shulkerFreeCapacityFor(item) > 0 }

fun Player.hasFullShopCommandView() =
    hasPermission(PermissionRegistry.SHOP_COMMAND_VIEW_ONLY_BYPASS)

fun Player.canUseFullShopView() = hasFullShopCommandView() || NpcShopState.isInNpcShop(uniqueId)

fun Player.canUseShopTransactionsFromCurrentView() =
    canUseFullShopView() || StaticShopState.isInStaticShop(uniqueId)

fun Player.canEditShopStorageFromCurrentView() =
    canUseFullShopView() || ChestShopEditState.getChest(uniqueId) != null

fun Player.canCreateShopFromCurrentView() =
    canUseFullShopView() || ChestShopEditState.getChest(uniqueId) != null

fun Player.canDeleteShopFromCurrentView() =
    canUseFullShopView() || ChestShopEditState.getChest(uniqueId) != null

fun Player.canEditShopPrice(shop: Shop) = shop.seller == uniqueId

fun Player.canUseShulkerFeature() = hasPermission(PermissionRegistry.SHULKER)
