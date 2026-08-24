package dev.slne.surf.shop.paper.menu.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockColumn
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.chest.ShopChestSelectShopView.initialState
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificPriceDialog
import dev.slne.surf.shop.paper.menu.canEditShopPrice
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

val priceEditView: AbstractSurfView = surfView("Preis bearbeiten") {
    val shopState = initialState<Shop>("edit-shop")
    val priceState = initialState<Double>("edit-price")

    settings {
        rows(5)
    }

    containerDefaults {
        blockRow(1)
        blockRow(5)
        blockColumn(0)
        blockColumn(8)
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
            if (!context.player.canEditShopPrice(shopState.get(this))) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis dieses Shops nicht bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.player.closeInventory()
            context.player.showDialog(
                createEditSpecificPriceDialog(
                    shopState.get(this).copy(pricePerItem = priceState.get(this))
                )
            )
        }

        layoutSlot('1', minusOne).onClick { context ->
            priceState.set(max(0.01, priceState.get(this) - 1), this)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('2', minusThirtyTwo).onClick { context ->
            priceState.set(max(0.01, priceState.get(this) - 50), this)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('3', plusOne).onClick { context ->
            priceState.set(priceState.get(this) + 1, this)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        layoutSlot('4', plusThirtyTwo).onClick { context ->
            priceState.set(priceState.get(this) + 50.0, this)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
            }
        }

        layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopPrice(shopState.get(this))) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis dieses Shops nicht bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.openForPlayer(
                editShopView::class.java,
                ImmutableMap.of(
                    "edit-shop",
                    shopState.get(this).copy(pricePerItem = priceState.get(context))
                )
            )
        }

        layoutSlot('P').watch(priceState).renderWith {
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