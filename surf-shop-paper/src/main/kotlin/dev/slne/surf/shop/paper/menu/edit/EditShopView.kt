package dev.slne.surf.shop.paper.menu.edit

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.core.util.updatedShop
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageView
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound

object EditShopView : View() {
    private val shopState = initialState<Shop>("edit-shop")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shop bearbeiten".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOIOOOO", "O       O", "O P F C O", "O       O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('P', pricePerItemItem.clone().apply {
            shopState.get(render).updatedShop?.pricePerItem?.let {
                if (it > 0) {
                    buildLore {
                        emptyLine()
                        line {
                            spacer("-")
                            appendSpace()
                            shopColored(
                                "Aktueller Preis Pro Item: ${
                                    formatPriceNice(
                                        shopState.get(
                                            render
                                        ).pricePerItem
                                    )
                                }"
                            )
                        }
                    }
                }
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                PriceEditView::class.java,
                ImmutableMap.of(
                    "edit-shop", shopState.get(render),
                    "edit-price", shopState.get(render).pricePerItem
                )
            )
        }

        render.layoutSlot('I', shopState.get(render).item.apply {
            amount = 1
        })

        render.layoutSlot('F', storageItem(shopState.get(render).storedItemCount))
            .onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    ItemStorageView::class.java,
                    ImmutableMap.of(
                        "edit-shop", shopState.get(render)
                    )
                )
            }

        render.layoutSlot('C', saveItem(render)).onClick { context ->
            context.playGeneralClickSound()

            val shop = shopState.get(context).updatedShop ?: run {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Der Shop existiert nicht mehr.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }
            val price = shop.pricePerItem

            if (price <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis des Shops nicht entfernen.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }

            plugin.launch {
                shopService.saveShop(shop)

                context.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde aktualisiert!")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    context.player.closeInventory()
                    viewFrame.open(
                        ShopListView::class.java,
                        context.player
                    )
                }
            }
        }
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()

            viewFrame.open(
                ShopListView::class.java,
                context.player
            )
        }
    }

    private fun saveItem(context: RenderContext) = MenuHeads.CHECK.clone().apply {
        displayName {
            shopColored("Speichern")
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                shopColored("Item: ")
                append(
                    Component.translatable(shopState.get(context).item.type.translationKey())
                        .color(Colors.VARIABLE_VALUE)
                )
            }

            line {
                spacer("-")
                appendSpace()
                shopColored("Preis pro Item: ")
                if (shopState.get(context).pricePerItem <= 0) {
                    variableValue("Kein Preis festgelegt")
                } else {
                    variableValue(formatPriceNice(shopState.get(context).pricePerItem))
                }
            }
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private fun storageItem(amount: Int) = buildItem(Material.CHEST) {
        displayName {
            shopColored("Item Lager")
        }

        buildLore {
            emptyLine()
            line {
                shopColored("Auf Lager: ")

                if (amount <= 0) {
                    error("Ausverkauft")
                } else {
                    variableValue("$amount Items")
                }
            }
        }
    }

    private val pricePerItemItem = MenuHeads.DOLLAR.clone().apply {
        displayName {
            shopColored("Preis pro Item festlegen")
        }
    }
}