package dev.slne.surf.shop.paper.menu.delete

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.chest.ShopChestSetupView
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration

object DeleteShopView : View() {
    private val shopState = initialState<Shop>("delete-shop")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shop löschen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "O   I C O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('B', MenuHeads.CROSS.clone().apply {
            displayName {
                error("Abbrechen")
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
                context.openForPlayer(ShopListView::class.java)
            }
        }

        val shop = shopState.get(render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', shop.item.clone())
        render.layoutSlot('C', MenuHeads.CHECK.clone().apply {
            displayName {
                success("Shop löschen")
            }
        }).onClick { context ->
            context.playGeneralClickSound()

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
                        context.openForPlayer(ShopListView::class.java)
                    }
                }
            }
        }
    }
}