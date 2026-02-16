package dev.slne.surf.shop.paper.menu.edit

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound

object EditAuctionView : View() {
    private val auctionState = initialState<Auction>("edit-auction")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Auktion bearbeiten".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOIOOOO", "O       O", "O P F C O", "O       O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('P', pricePerItemItem.clone().apply {
            if (auctionState.get(render).pricePerItem > 0) {
                buildLore {
                    emptyLine()
                    line {
                        spacer("-")
                        appendSpace()
                        auctionColored("Aktueller Preis Pro Item: ${auctionState.get(render).pricePerItem}")
                    }
                }
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                PriceEditView::class.java,
                ImmutableMap.of(
                    "edit-auction", auctionState.get(render),
                    "edit-price", auctionState.get(render).pricePerItem
                )
            )
        }

        render.layoutSlot('I', auctionState.get(render).item.apply {
            amount = 1
        })

        render.layoutSlot('F', insertItemsItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                ItemStorageView::class.java,
                ImmutableMap.of(
                    "edit-auction", auctionState.get(render)
                )
            )
        }

        render.layoutSlot('C', saveItem(render)).onClick { context ->
            context.playGeneralClickSound()

            val auction = auctionState.get(context)
            val price = auction.pricePerItem

            if (price <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis der Auktion nicht entfernen.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }

            plugin.launch {
                auctionService.saveAuction(auctionState.get(render))

                context.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Die Auktion wurde aktualisiert!")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    context.player.closeInventory()
                    viewFrame.open(
                        AuctionListView::class.java,
                        context.player
                    )
                }
            }
        }
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()

            viewFrame.open(
                AuctionListView::class.java,
                context.player
            )
        }
    }

    private fun saveItem(context: RenderContext) = MenuHeads.CHECK.clone().apply {
        displayName {
            auctionColored("Speichern")
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                auctionColored("Item: ")
                append(
                    Component.translatable(auctionState.get(context).item.type.translationKey())
                        .color(Colors.VARIABLE_VALUE)
                )
            }

            line {
                spacer("-")
                appendSpace()
                auctionColored("Preis pro Item: ")
                if (auctionState.get(context).pricePerItem <= 0) {
                    variableValue("Kein Preis festgelegt")
                } else {
                    variableValue(auctionState.get(context).pricePerItem)
                }
            }
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val insertItemsItem = buildItem(Material.CHEST) {
        displayName {
            auctionColored("Items lagern")
        }
    }

    private val pricePerItemItem = MenuHeads.DOLLAR.clone().apply {
        displayName {
            auctionColored("Preis pro Item festlegen")
        }
    }
}