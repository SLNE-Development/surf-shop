package dev.slne.surf.shop.paper.menu.select

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.shop.paper.dialog.create.createSpecificPriceDialog
import dev.slne.surf.shop.paper.menu.createShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import kotlin.math.max

val priceSelectView: AbstractSurfView = surfView("Preis festlegen") {
    val itemState = initialState<ItemStack>("create-item")
    val priceState = initialState<Double>("create-price")

    settings {
        rows(5)
        cancelAllInteractions()
    }

    onInit {
        layout(
            "OOOOOOOOO",
            "O   W   O",
            "O21 P 34O",
            "O       O",
            "OOOOBOOOO"
        )
    }

    onFirstRender {
        layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()
            context.player.showDialog(
                createSpecificPriceDialog(
                    itemState[this],
                    priceState[this]
                )
            )
        }

        layoutSlot('1', minusOne).onClick { context ->
            priceState[this] = max(0.01, priceState[this] - 1)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('2', minusThirtyTwo).onClick { context ->
            priceState[this] = max(0.01, priceState[this] - 50)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('3', plusOne).onClick { context ->
            priceState[this] = priceState[this] + 1
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        layoutSlot('4', plusThirtyTwo).onClick { context ->
            priceState[this] = priceState[this] + 50
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                createShopView::class.java,
                ImmutableMap.of(
                    "create-item",
                    itemState[this],
                    "create-price",
                    priceState[this]
                )
            )
        }

        layoutSlot('P').renderWith {
            valueItem(priceState[this])
        }
    }
}

private fun valueItem(price: Double) = buildItem(Material.GOLD_INGOT) {
    displayName {
        shopColored("Preis: ", TextDecoration.BOLD)
        appendSpace()
        shopColored(formatPriceNice(price))
    }
}

private val plusOne = MenuHeads.PLUS.clone().apply {
    displayName { shopColored("+1") }
}

private val plusThirtyTwo = MenuHeads.PLUS.clone().apply {
    displayName { shopColored("+50") }
}

private val minusOne = MenuHeads.MINUS.clone().apply {
    displayName { shopColored("-1") }
}

private val minusThirtyTwo = MenuHeads.MINUS.clone().apply {
    displayName { shopColored("-50") }
}

private val continueItem = MenuHeads.CHECK.clone().apply {
    displayName { shopColored("Übernehmen") }
}

private val ownItem = MenuHeads.DOLLAR.clone().apply {
    displayName { shopColored("Eigenen Preis eingeben") }
}
