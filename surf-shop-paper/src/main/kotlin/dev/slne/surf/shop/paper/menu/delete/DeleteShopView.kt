package dev.slne.surf.shop.paper.menu.delete

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.chest.ShopChestSetupView
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import kotlinx.coroutines.withContext

val deleteShopView: AbstractSurfView = surfView("Shop löschen") {
    val shopState = initialState<Shop>("delete-shop")

    settings {
        rows(3)
    }

    containerDefaults {
        blockRow(1)
        blockRow(2)
        blockRow(3)
    }

    onFirstRender {
        slot(2, 3, ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
            displayName {
                error("Abbrechen".toSmallCaps())
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            val chest = ChestShopEditState.getChest(context.player.uniqueId)
            if (chest != null) {
                context.player.closeInventory()
                viewFrame.open(
                    ShopChestSetupView::class.java,
                    context.player,
                    ImmutableMap.of("shop-chest", chest)
                )
            } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                context.openForPlayer(OwnShopsListView::class.java)
            } else {
                context.openForPlayer(shopListView::class.java)
            }
        }

        val shop = shopState[this]

        slot(1, 5, shop.item.clone())
        slot(2, 5, ViewIcon(ViewIconType.CHECK, ViewIconColor.GREEN).build {
            displayName {
                success("Shop löschen")
            }
        }).onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canDeleteShopFromCurrentView()) {
                context.player.sendText {
                    appendInfoPrefix()
                    info("Shops kannst du nur am Spawn löschen.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.player.sendText {
                appendInfoPrefix()
                info("Der Shop wird gelöscht...")
            }

            if (shop.storedItemCount > 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Der Shop enthält noch ${shop.storedItemCount} gelagerte Items. Bitte entferne diese zuerst, bevor du den Shop löschen kannst.")
                }
                return@onClick
            }

            plugin.launch {
                ShopService.deleteShop(shop)

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logDelete(context.player, shop)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde erfolgreich gelöscht.")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    val chest = ChestShopEditState.getChest(context.player.uniqueId)
                    if (chest != null) {
                        context.player.closeInventory()
                        viewFrame.open(
                            ShopChestSetupView::class.java,
                            context.player,
                            ImmutableMap.of("shop-chest", chest)
                        )
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(OwnShopsListView::class.java)
                    } else {
                        context.openForPlayer(shopListView::class.java)
                    }
                }
            }
        }
    }
}
