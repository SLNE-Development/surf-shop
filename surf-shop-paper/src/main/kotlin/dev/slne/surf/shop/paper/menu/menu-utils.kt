package dev.slne.surf.shop.paper.menu

import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import me.devnatan.inventoryframework.View
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val View.outlineItem: ItemStack
    get() = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }