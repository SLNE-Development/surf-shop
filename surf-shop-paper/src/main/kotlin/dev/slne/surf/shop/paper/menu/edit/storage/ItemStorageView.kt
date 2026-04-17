package dev.slne.surf.shop.paper.menu.edit.storage

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.MenuHeads
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration

object ItemStorageView : View() {
    private val shopState = initialState<Shop>("edit-shop")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Item Lager".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "OOOAOCOOO", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()

            viewFrame.open(
                EditShopView::class.java,
                context.player,
                ImmutableMap.of("edit-shop", shopState.get(render))
            )
        }

        render.layoutSlot('A', insertItemsItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                ItemStorageInsertView::class.java,
                ImmutableMap.of(
                    "edit-shop", shopState.get(render)
                )
            )
        }

        render.layoutSlot('C', removeItemsNotAvailable).displayIf { context ->
            ChestShopEditState.getChest(context.player.uniqueId) != null
        }

        render.layoutSlot('C', removeItemsItem).displayIf { context ->
            ChestShopEditState.getChest(context.player.uniqueId) == null
        }.onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                ItemStorageRemoveView::class.java,
                ImmutableMap.of(
                    "edit-shop", shopState.get(render),
                    "edit-amount", 0
                )
            )
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
}