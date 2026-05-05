package dev.slne.surf.shop.paper.menu

import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
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