package dev.slne.surf.shop.paper.menu.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.chest.ShopChestSelectShopView.initialState
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificPriceDialog
import dev.slne.surf.shop.paper.menu.canEditShopPrice
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.formatPriceNice
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

val priceEditView: AbstractSurfView = surfView("Preis bearbeiten") {
    val shopState = initialState<Shop>("edit-shop")
    val priceState = initialState<Double>("edit-price")

    settings {
        rows(4)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(4)
    }

    onInit {
        layout(
            "    W    ",
            " 21 P 34 ",
            "         ",
            "#       B"
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

        layoutSlot('#', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                editShopView::class.java,
                ImmutableMap.of(
                    "edit-shop",
                    shopState.get(this)
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

private val plusOne = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName { shopColored("+1") }
}

private val plusThirtyTwo = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName { shopColored("+50") }
}

private val minusOne = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
    displayName { shopColored("-1") }
}

private val minusThirtyTwo = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
    displayName { shopColored("-50") }
}

private val backItem = ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
    displayName { error("Zurück") }
}

private val continueItem = ViewIcon(ViewIconType.CHECK, ViewIconColor.GREEN).build {
    displayName { success("Übernehmen") }
}

private val ownItem = ViewIcon(ViewIconType.BELL, ViewIconColor.YELLOW).build {
    displayName { shopColored("Eigenen Preis eingeben") }
}