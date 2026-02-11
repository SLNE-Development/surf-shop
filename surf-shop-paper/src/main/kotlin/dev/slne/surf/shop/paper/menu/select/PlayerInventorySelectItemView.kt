package dev.slne.surf.shop.paper.menu.select

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.paper.menu.CreateAuctionView
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
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

object PlayerInventorySelectItemView : View() {
    private val priceState: State<Int> = initialState("create-price")

    private val paginationState = buildComputedPaginationState<ItemStack> { context ->
        context.player.inventory.storageContents.filterNotNull().toMutableList()
    }.itemFactory { builder, item ->
        builder.withItem(item).onClick { context ->
            context.openForPlayer(
                CreateAuctionView::class.java,
                ImmutableMap.of(
                    "create-item",
                    item,
                    "create-price",
                    priceState.get(context)
                )
            )
        }
    }.layoutTarget('I').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.titleBuilder {
            auctionColored("Item wählen".toSmallCaps(), TextDecoration.BOLD)
        }
            .size(6)
            .layout(
                "XXXXQXXXX",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "XXXXBXXXX"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)
        render.layoutSlot('X', outlineItem)
        render.layoutSlot('Q', explainItem)
        render.layoutSlot('B', backItem).onClick { context ->
            context.back()
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val explainItem = MenuHeads.QUESTION.clone().apply {
        displayName {
            auctionColored("Erklärung".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                auctionColored("Klicke auf ein Item, um es auszuwählen.")
            }
            line {
                spacer("-")
                appendSpace()
                auctionColored("Nach der Auswahl kommst du in das Vorschau-Menü,")
            }
            line {
                appendSpace()
                appendSpace()
                appendSpace()
                auctionColored("in dem du deine Auktion erstellen kannst.")
            }
        }
    }
}