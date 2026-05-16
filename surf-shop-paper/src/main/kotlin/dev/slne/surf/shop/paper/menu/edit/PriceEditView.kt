package dev.slne.surf.shop.paper.menu.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificPriceDialog
import dev.slne.surf.shop.paper.menu.canEditShopPrice
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.settings.SettingsHook
import dev.slne.surf.shop.paper.settings.hasSettingsApi
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

object PriceEditView : View() {
    private val shopState = initialState<Shop>("edit-shop")
    private val priceState: State<Double> = initialState("edit-price")
    private val localPriceState = mutableState(0.01)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Preis bearbeiten".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O   W   O",
                "O21 P 34O",
                "O       O",
                "OOOOBOOOO"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        localPriceState.set(priceState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopPrice(shopState.get(render))) {
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
                    shopState.get(render).copy(pricePerItem = localPriceState.get(render))
                )
            )
        }

        render.layoutSlot('1', minusOne).onClick { context ->
            localPriceState.set(max(0.01, localPriceState.get(render) - 1), render)
            context.update()

            if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
                }
            }
        }

        render.layoutSlot('2', minusThirtyTwo).onClick { context ->
            localPriceState.set(max(0.01, localPriceState.get(render) - 50), render)
            context.update()

            if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
                }
            }
        }

        render.layoutSlot('3', plusOne).onClick { context ->
            localPriceState.set(localPriceState.get(render) + 1, render)
            context.update()

            if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
                }
            }
        }

        render.layoutSlot('4', plusThirtyTwo).onClick { context ->
            localPriceState.set(localPriceState.get(render) + 50, render)
            context.update()

            if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
                }
            }
        }

        render.layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopPrice(shopState.get(render))) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis dieses Shops nicht bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.openForPlayer(
                EditShopView::class.java,
                ImmutableMap.of(
                    "edit-shop",
                    shopState.get(render).copy(pricePerItem = localPriceState.get(context))
                )
            )
        }

        render.layoutSlot('P').watch(localPriceState).renderWith {
            valueItem(render)
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private fun valueItem(context: RenderContext) = buildItem(Material.GOLD_INGOT) {
        displayName {
            shopColored("Preis: ", TextDecoration.BOLD)
            appendSpace()
            shopColored(formatPriceNice(localPriceState.get(context)))
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
}
