package dev.slne.surf.shop.paper.menu.edit

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration

object ItemStorageView : View() {
    private val auctionState = initialState<Auction>("edit-auction")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Item Lager".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "OOOAOCOOO", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.back()
        }

        render.layoutSlot('A', insertItemsItem)
        render.layoutSlot('C', removeItemsItem)
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val insertItemsItem = MenuHeads.PLUS.clone().apply {
        displayName {
            error("Items einlagern")
        }
    }

    private val removeItemsItem = MenuHeads.MINUS.clone().apply {
        displayName {
            error("Items auslagern")
        }
    }
}