package dev.slne.surf.shop.paper.menu.edit.storage

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound

val itemStorageView: AbstractSurfView = surfView("Item Lager") {
    val shopState = initialState<Shop>("edit-shop")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(3)
    }

    onInit {
        layout("         ", "   A C   ", "B        ")
    }

    onFirstRender {
        layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()

            viewFrame.open(
                editShopView::class.java,
                context.player,
                ImmutableMap.of("edit-shop", shopState.get(this))
            )
        }

        layoutSlot('A', lockedStorageItem).displayIf { context ->
            !context.player.canEditShopStorageFromCurrentView()
        }

        layoutSlot('A', insertItemsItem).displayIf { context ->
            context.player.canEditShopStorageFromCurrentView()
        }.onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.openForPlayer(
                itemStorageInsertView::class.java,
                ImmutableMap.of(
                    "edit-shop", shopState.get(this)
                )
            )
        }

        layoutSlot('C', lockedStorageItem).displayIf { context ->
            !context.player.canEditShopStorageFromCurrentView()
        }

        layoutSlot('C', removeItemsNotAvailable).displayIf { context ->
            ChestShopEditState.getChest(context.player.uniqueId) != null
        }

        layoutSlot('C', removeItemsItem).displayIf { context ->
            ChestShopEditState.getChest(context.player.uniqueId) == null && context.player.canEditShopStorageFromCurrentView()
        }.onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.openForPlayer(
                itemStorageRemoveView::class.java,
                ImmutableMap.of(
                    "edit-shop", shopState.get(this),
                    "edit-amount", 0
                )
            )
        }
    }
}

private val backItem = ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
    displayName {
        error("Zurück")
    }
}

private val insertItemsItem = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName {
        success("Items einlagern")
    }
}

private val removeItemsItem = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
    displayName {
        error("Items auslagern")
    }
}

private val removeItemsNotAvailable = ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
    displayName {
        error("Items auslagern")
    }

    buildLore {
        emptyLine()
        line {
            error("Diese Funktion ist in Shop Kisten nicht verfügbar.")
        }
    }
}

private val lockedStorageItem = ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
    displayName {
        error("Nur am Spawn")
    }

    buildLore {
        emptyLine()
        line {
            error("Das Lager kannst du nur am Spawn bearbeiten.")
        }
    }
}