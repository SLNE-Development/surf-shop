package dev.slne.surf.shop.paper.menu.delete

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
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
            if (OwnShopState.isInOwn(context.player.uniqueId)) {
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
                shopService.deleteShop(shop)

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logDelete(context.player, shop)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde erfolgreich gelöscht.")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(OwnShopsListView::class.java)
                    } else {
                        context.openForPlayer(ShopListView::class.java)
                    }
                }
            }
        }
    }
}