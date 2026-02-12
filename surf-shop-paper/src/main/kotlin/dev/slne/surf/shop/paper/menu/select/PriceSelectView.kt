package dev.slne.surf.shop.paper.menu.select

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.paper.dialog.create.createSpecificPriceDialog
import dev.slne.surf.shop.paper.menu.CreateAuctionView
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import kotlin.math.max

object PriceSelectView : View() {
    private val itemState: State<ItemStack> = initialState("create-item")
    private val priceState: State<Int> = initialState("create-price")
    private val localPriceState = mutableState(0)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Preis festlegen".toSmallCaps(), TextDecoration.BOLD)
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
        localPriceState.set(priceState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()
            context.player.showDialog(
                createSpecificPriceDialog(
                    itemState.get(render),
                    localPriceState.get(render)
                )
            )
        }

        render.layoutSlot('1', minusOne).onClick { context ->
            localPriceState.set(max(0, localPriceState.get(render) - 1), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('2', minusThirtyTwo).onClick { context ->
            localPriceState.set(max(0, localPriceState.get(render) - 50), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('3', plusOne).onClick { context ->
            localPriceState.set(localPriceState.get(render) + 1, render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        render.layoutSlot('4', plusThirtyTwo).onClick { context ->
            localPriceState.set(localPriceState.get(render) + 50, render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        render.layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                CreateAuctionView::class.java,
                ImmutableMap.of(
                    "create-item",
                    itemState.get(render),
                    "create-price",
                    localPriceState.get(context)
                )
            )
        }

        render.layoutSlot('P').watch(localPriceState).renderWith {
            valueItem(render)
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private fun valueItem(context: RenderContext) = buildItem(Material.GOLD_INGOT) {
        displayName {
            auctionColored("Preis: ", TextDecoration.BOLD)
            appendSpace()
            auctionColored(localPriceState.get(context))
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
        displayName { auctionColored("Übernehmen") }
    }

    private val ownItem = MenuHeads.DOLLAR.clone().apply {
        displayName { auctionColored("Eigenen Preis eingeben") }
    }
}
