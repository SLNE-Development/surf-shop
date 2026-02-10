package dev.slne.surf.shop.paper.menu

import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CreateAuctionView : View() {
    private val itemState: State<ItemStack?> = mutableState(null)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Auktion erstellen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOOOOOO", "O       O", "O   I C O", "O       O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', itemNotSet)
        render.layoutSlot('C', createItem)
        render.layoutSlot('B', backItem).onClick { context ->
            context.back()
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val createItem = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            auctionColored("Auktion erstellen")
        }
    }

    private val itemNotSet = MenuHeads.QUESTION.clone().apply {
        displayName {
            auctionColored("Kein Item ausgewählt")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }
}