package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.translatable
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

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

    override fun onClick(click: SlotClickContext) {
        if (click.clickedContainer.isEntityContainer) {
            if (click.isShiftLeftClick) {
                val item = click.item
                val auction = auctionState.get(click)

                if (!item.isSimilar(auction.item)) {
                    return
                }

                click.isCancelled = false
                click.clickOrigin.currentItem = ItemStack.empty()

                plugin.launch {
                    val amount = item.amount
                    auctionService.saveAuction(auction.copy(storedItemCount = auction.storedItemCount + amount))

                    click.player.sendText {
                        appendSuccessPrefix()
                        success("Du hast ")
                        variableValue("${amount}x ")
                        translatable(item.translationKey())
                        success(" eingelagert.")
                    }
                }
            }
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