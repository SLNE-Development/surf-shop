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
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.CloseContext
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import me.devnatan.inventoryframework.state.MutableState
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

object ItemStorageInsertView : View() {
    private val auctionState = initialState<Auction>("edit-auction")
    private val localAuctionState: MutableState<Auction> = mutableState(Auction.empty())
    private val itemInsertedState = mutableState(0)

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
        localAuctionState.set(auctionState.get(render), render)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()

            context.openForPlayer(
                ItemStorageView::class.java,
                ImmutableMap.of("edit-auction", auctionState.get(render))
            )
        }

        render.layoutSlot('S', ItemStack.empty()).onClick { context ->
            context.isCancelled = false
        }

        render.layoutSlot('Q').renderWith {
            explainItem(localAuctionState.get(render).storedItemCount)
        }.watch(localAuctionState)
    }

    override fun onClick(click: SlotClickContext) {
        if (click.clickedContainer.isEntityContainer) {
            if (click.isShiftLeftClick) {
                val item = click.item ?: return
                val auction = localAuctionState.get(click)

                if (!item.isSimilar(auction.item)) {
                    return
                }

                click.isCancelled = false
                click.clickOrigin.currentItem = ItemStack.empty()

                plugin.launch {
                    val amount = item.amount
                    val newAuction =
                        auction.copy(storedItemCount = auction.storedItemCount + amount)
                    localAuctionState.set(newAuction, click)

                    auctionService.saveAuction(newAuction)
                    itemInsertedState.set(itemInsertedState.get(click) + amount, click)

                    click.player.sendActionBar(buildText {
                        appendSuccessPrefix()
                        success("Du hast ")
                        variableValue("$amount Items")
                        success(" eingelagert.")
                    })

                    click.player.playSound(true) {
                        type(Sound.ENTITY_PLAYER_LEVELUP)
                    }
                }
            }
        }
    }

    override fun onClose(close: CloseContext) {
        val added = itemInsertedState.get(close)
        if (added > 0) {
            close.player.sendText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("${added}x ")
                translatable(auctionState.get(close).item.type.translationKey())
                success(" eingelagert.")
            }

            close.player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_YES)
            }
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Zurück")
        }
    }

    private fun explainItem(amount: Int) = MenuHeads.QUESTION.clone().apply {
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
            emptyLine()
            line {
                spacer("Derzeit sind ")
                variableValue("$amount Items")
                spacer(" im Lager.")
            }
        }
    }
}