package dev.slne.surf.shop.paper.menu.edit.storage

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration

object ItemStorageInsertView : View() {
    private val auctionState = initialState<Auction>("edit-auction")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Items einlagern".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOQOOOO", "OSSSSSSSO", "OSSSSSSSO", "OSSSSSSSO", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()

            viewFrame.open(
                ItemStorageView::class.java,
                context.player,
                ImmutableMap.of("edit-auction", auctionState.get(render))
            )
        }

        render.layoutSlot('S').onClick { context ->
            context.isCancelled = false
        }
        render.layoutSlot('Q', explainItem)
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val insertItemsItem = MenuHeads.PLUS.clone().apply {
        displayName {
            success("Items einlagern")
        }
    }

    private val removeItemsItem = MenuHeads.MINUS.clone().apply {
        displayName {
            error("Items auslagern")
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
                auctionColored("Klicke auf ein Item, um es einzulagern.")
            }
            line {
                spacer("-")
                appendSpace()
                auctionColored("Das ausgewählte Item wird sofort in deine Auktion eingelagert")
            }
            line {
                appendSpace()
                appendSpace()
                appendSpace()
                auctionColored("und steht zum Verkauf bereit.")
            }
        }
    }
}