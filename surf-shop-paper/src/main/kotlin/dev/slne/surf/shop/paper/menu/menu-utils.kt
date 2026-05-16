package dev.slne.surf.shop.paper.menu

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import dev.slne.surf.shop.paper.settings.SettingsHook
import dev.slne.surf.shop.paper.settings.hasSettingsApi
import me.devnatan.inventoryframework.View
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val View.outlineItem: ItemStack
    get() = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

fun Player.playNoSound() {
    if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(uniqueId)) {
        this.playSound(true) {
            type(Sound.ENTITY_VILLAGER_NO)
        }
    }
}

fun Player.hasFullShopCommandView() = hasPermission(PermissionRegistry.SHOP_COMMAND_VIEW_ONLY_BYPASS)

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
