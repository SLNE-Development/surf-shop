package dev.slne.surf.shop.paper.menu

import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object PlayerInventorySelectItemView : View() {
    private val paginationState = buildComputedPaginationState<ItemStack> { context ->
        context.player.inventory.storageContents.filterNotNull().toMutableList()
    }.itemFactory { builder, item ->
        builder.withItem(item)
    }.layoutTarget('I').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.titleBuilder {
            auctionColored("Item wählen".toSmallCaps(), TextDecoration.BOLD)
        }
            .size(6)
            .layout(
                "XXXXXXXXX",
                "XIIIIIIIX",
                "XIIIIIIIX",
                "XIIIIIIIX",
                "XIIIIIIIX",
                "XXBXXXSXX"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('X', outlineItem)
        render.layoutSlot('B', backItem).onClick { context ->
            context.back()
        }
        render.layoutSlot('S', continueItem)
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val continueItem = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            auctionColored("Fortfahren")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }
}