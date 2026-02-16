package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificRemoveAmountPriceDialog
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

object ItemStorageRemoveView : View() {
    private val auctionState = initialState<Auction>("edit-auction")
    private val amountState = initialState<Int>("edit-amount")
    private val localAmountState = mutableState(0)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Anzahl auswählen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O   W   O",
                "O21 P 34O",
                "O       O",
                "OOOOBOOOO"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        localAmountState.set(amountState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()
            context.player.showDialog(
                createEditSpecificRemoveAmountPriceDialog(
                    auctionState.get(render)
                )
            )
        }

        render.layoutSlot('1', minusOne).onClick { context ->
            localAmountState.set(max(0, localAmountState.get(render) - 1), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('2', minusThirtyTwo).onClick { context ->
            localAmountState.set(max(0, localAmountState.get(render) - 50), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('3', plusOne).onClick { context ->
            localAmountState.set(localAmountState.get(render) + 1, render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        render.layoutSlot('4', plusThirtyTwo).onClick { context ->
            localAmountState.set(localAmountState.get(render) + 50, render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        render.layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()

            val toRemove = localAmountState.get(context)

            if (toRemove <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du musst mindestens 1 Item entfernen.")
                }
                return@onClick
            }

            plugin.launch {
                val auction = auctionState.get(context)
                auctionService.blockAuctionAction(auction)

                val updatedAuction =
                    auctionService.loadedAuctions.find { it.auctionUuid === auction.auctionUuid }

                if (updatedAuction == null) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Die Auktion existiert nicht mehr.")
                    }
                    return@launch
                }

                if (updatedAuction.storedItemCount < toRemove) {
                    auctionService.saveAuction(updatedAuction.copy(storedItemCount = 0))

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Die Auktion wurde aktualisiert. Es konnten aber nur ")
                        variableValue(updatedAuction.storedItemCount)
                        success(" von ")
                        variableValue(toRemove)
                        success(" Items entfernt werden, da die Auktion nur noch ")
                        variableValue(updatedAuction.storedItemCount)
                        success(" Items gespeichert hatte.")
                    }
                } else {
                    auctionService.saveAuction(updatedAuction.copy(storedItemCount = updatedAuction.storedItemCount - toRemove))

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Die Auktion wurde aktualisiert und ")
                        variableValue(toRemove)
                        success(" Items wurden entfernt. Es sind nun noch ")
                        variableValue(updatedAuction.storedItemCount - toRemove)
                        success(" Items in der Auktion gespeichert.")
                    }
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    context.openForPlayer(
                        ItemStorageView::class.java,
                        ImmutableMap.of(
                            "edit-auction",
                            auctionState.get(render)
                                .copy(pricePerItem = localAmountState.get(context))
                        )
                    )
                }
            }
        }

        render.layoutSlot('P').watch(localAmountState).renderWith {
            valueItem(render)
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private fun valueItem(context: RenderContext) = buildItem(Material.GOLD_INGOT) {
        displayName {
            auctionColored("Anzahl: ", TextDecoration.BOLD)
            appendSpace()
            auctionColored((" ${localAmountState.get(context)}/" + auctionService.loadedAuctions.find {
                it.auctionUuid == auctionState.get(
                    context
                )?.auctionUuid
            }?.storedItemCount))
        }
    }

    private val plusOne = MenuHeads.PLUS.clone().apply {
        displayName { auctionColored("+1") }
    }

    private val plusThirtyTwo = MenuHeads.PLUS.clone().apply {
        displayName { auctionColored("+50") }
    }

    private val minusOne = MenuHeads.MINUS.clone().apply {
        displayName { auctionColored("-1") }
    }

    private val minusThirtyTwo = MenuHeads.MINUS.clone().apply {
        displayName { auctionColored("-50") }
    }

    private val continueItem = MenuHeads.CHECK.clone().apply {
        displayName { auctionColored("Auszahlen") }
    }

    private val ownItem = MenuHeads.DOLLAR.clone().apply {
        displayName { auctionColored("Eigene Anzahl eingeben") }
    }
}
