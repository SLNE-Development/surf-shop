package dev.slne.surf.shop.paper.menu.edit.storage

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockColumn
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.chest.ShopChestSelectShopView.initialState
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.util.MenuHeads

val itemStorageView: AbstractSurfView = surfView("Item Lager") {
    val shopState = initialState<Shop>("edit-shop")

    settings {
        rows(3)
    }

    containerDefaults {
        blockRow(1)
        blockColumn(0)
        blockColumn(8)
        blockRow(3)
    }

    onInit {
        layout("OOOOOOOOO", "OOOAOCOOO", "OOOOBOOOO")
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

private val backItem = MenuHeads.CROSS.clone().apply {
    displayName {
        error("Abbrechen")
    }
}

private val insertItemsItem = MenuHeads.PLUS.clone().apply {
    displayName {
        success("Items einlagern")
    }
}

private val removeItemsItem = MenuHeads.MINUS.clone().apply {
    displayName {
        error("Items auslagern")
    }
}

private val removeItemsNotAvailable = MenuHeads.CROSS.clone().apply {
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

private val lockedStorageItem = MenuHeads.CROSS.clone().apply {
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