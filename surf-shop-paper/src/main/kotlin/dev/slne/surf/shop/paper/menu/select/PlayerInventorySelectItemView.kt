package dev.slne.surf.shop.paper.menu.select

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.shop.core.paper.util.isAllowedToSell
import dev.slne.surf.shop.paper.menu.createShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.MenuHeads
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

val playerInventorySelectItemView: AbstractSurfView = paginatedSurfView("Item wählen") {
    val priceState = initialState<Double>("create-price")
    val itemState = initialState<ItemStack>("create-item")

    settings {
        paginationEmptyRows(1)
        paginationViewRows(PaginationViewRows.FOUR)
        cancelAllInteractions()
        navigateBackOnOutsideClick(false)
    }

    pagination {
        computedSource { context ->
            context.player.inventory.storageContents.filterNotNull().toMutableList()
        }

        itemFactory { item ->
            withItem(item).onClick { context ->
                if (!isAllowedToSell(item)) {
                    context.player.playSound(true) {
                        type(Sound.BLOCK_NOTE_BLOCK_BASS)
                        pitch(2f)
                    }
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieses Item kann nicht verkauft werden.")
                    }
                    return@onClick
                }

                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_PLING)
                    pitch(2f)
                }

                context.openForPlayer(
                    createShopView::class.java,
                    ImmutableMap.of(
                        "create-item",
                        item.clone().apply {
                            amount = 1
                        },
                        "create-price",
                        0.0
                    )
                )
            }
        }
    }

    layoutTarget('I')

    containerDefaults {
        blockRow(1)
    }

    onFirstRender {
        slot(1, 5, explainItem)
        slot(5, 1, backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                createShopView::class.java, ImmutableMap.of(
                    "create-price", priceState[context],
                    "create-item", itemState[context]
                )
            )
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
        shopColored("Erklärung".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        emptyLine()
        line {
            spacer("-")
            appendSpace()
            shopColored("Klicke auf ein Item, um es auszuwählen.")
        }
        line {
            spacer("-")
            appendSpace()
            shopColored("Nach der Auswahl kommst du in das Vorschau-Menü,")
        }
        line {
            appendSpace()
            appendSpace()
            appendSpace()
            shopColored("in dem du deinen Shop erstellen kannst.")
        }
    }
}