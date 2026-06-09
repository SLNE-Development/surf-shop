package dev.slne.surf.shop.paper.menu.edit

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.MAX_PRICE
import dev.slne.surf.shop.core.paper.util.MIN_PRICE
import dev.slne.surf.shop.core.paper.util.isValidePrice
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.chest.ShopChestSetupView
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.formatPriceNice
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
        val canEditStorage = render.player.canEditShopStorageFromCurrentView()

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('P', pricePerItemItem.clone().apply {
            if (shopState.get(render).pricePerItem > 0) {
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
        }).onClick { context ->
            context.playGeneralClickSound()

            val shop = shopState.get(render)
            if (!context.player.canEditShopPrice(shop)) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Preis dieses Shops nicht bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.openForPlayer(
                PriceEditView::class.java,
                ImmutableMap.of(
                    "edit-shop", shop,
                    "edit-price", shop.pricePerItem
                )
            )
        }

        render.layoutSlot('I', shopState.get(render).item.apply {
            amount = 1
        })

        render.layoutSlot(
            'F',
            if (canEditStorage) storageItem(shopState.get(render).storedItemCount) else lockedStorageItem(
                shopState.get(render).storedItemCount
            )
        )
            .onClick { context ->
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
                    ItemStorageView::class.java,
                    ImmutableMap.of(
                        "edit-shop", shopState.get(render)
                    )
                )
            }

        render.layoutSlot('C', saveItem(render)).onClick { context ->
            context.playGeneralClickSound()

            val shop = shopState.get(context)
            val price = shop.pricePerItem

            if (!context.player.canEditShopPrice(shop)) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst diesen Shop nicht bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            if (!isValidePrice(price)) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Der angegebene Preis kann nicht angewendet werden. Er muss zwischen")
                    appendSpace()
                    variableValue(MIN_PRICE)
                    appendSpace()
                    error("und")
                    appendSpace()
                    variableValue(MAX_PRICE)
                    appendSpace()
                    error("liegen.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }

            plugin.launch {
                ShopService.saveShop(shopState.get(render))

                context.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde aktualisiert!")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    context.player.closeInventory()
                    val chest = ChestShopEditState.getChest(context.player.uniqueId)
                    if (chest != null) {
                        viewFrame.open(
                            ShopChestSetupView::class.java,
                            context.player,
                            ImmutableMap.of("shop-chest", chest)
                        )
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        viewFrame.open(OwnShopsListView::class.java, context.player)
                    } else {
                        viewFrame.open(ShopListView::class.java, context.player)
                    }
                }
            }
        }
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()

            val chest = ChestShopEditState.getChest(context.player.uniqueId)
            if (chest != null) {
                viewFrame.open(
                    ShopChestSetupView::class.java,
                    context.player,
                    ImmutableMap.of("shop-chest", chest)
                )
            } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                viewFrame.open(OwnShopsListView::class.java, context.player)
            } else {
                viewFrame.open(ShopListView::class.java, context.player)
            }
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

    private fun lockedStorageItem(amount: Int) = buildItem(Material.BARRIER) {
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

            emptyLine()
            line {
                appendBlob()
                spacer("Das Lager kannst du nur am Spawn bearbeiten.".toSmallCaps())
            }
        }
    }

    private val pricePerItemItem = MenuHeads.DOLLAR.clone().apply {
        displayName {
            shopColored("Preis pro Item festlegen")
        }
    }
}
