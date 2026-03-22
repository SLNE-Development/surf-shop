package dev.slne.surf.shop.paper.menu

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
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
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

object CreateShopView : View() {
    private val itemState: State<ItemStack> = initialState("create-item")
    private val priceState: State<Int> = initialState("create-price")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shop erstellen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOOOOOO", "O       O", "O P I C O", "O       O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('P', pricePerItemItem.clone().apply {
            if (priceState.get(render) > 0) {
                buildLore {
                    emptyLine()
                    line {
                        spacer("-")
                        appendSpace()
                        shopColored("Aktueller Preis Pro Item: ${priceState.get(render)}")
                    }
                }
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                PriceSelectView::class.java,
                ImmutableMap.of(
                    "create-item", itemState.get(render),
                    "create-price", priceState.get(render)
                )
            )
        }

        if (itemState.get(render)?.isEmpty == true) {
            render.layoutSlot('I', itemNotSet).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState.get(context)
                    )
                )
            }
        } else {
            render.layoutSlot('I', itemState.get(render).clone().apply {
                amount = 1
            }).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState.get(context)
                    )
                )
            }
        }

        render.layoutSlot('C', createItem(render)).onClick { context ->
            context.playGeneralClickSound()

            val item = itemState.get(context)
            val price = priceState.get(context)

            if (item.isEmpty) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du musst ein Item auswählen, um ein Shop zu erstellen.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }

            if (price <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du musst einen Preis pro Item festlegen, um ein Shop zu erstellen.")
                }
                context.player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
                return@onClick
            }

            plugin.launch {
                val shop = shopService.createShop(item.clone().apply {
                    amount = 1
                }, 0, price, context.player.uniqueId)

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logCreate(context.player, shop)
                }

                context.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde erstellt!")
                }

                withContext(plugin.globalRegionDispatcher) {
                    context.player.closeInventory()
                    viewFrame.open(
                        EditShopView::class.java,
                        context.player,
                        ImmutableMap.of("edit-shop", shop)
                    )
                }
            }
        }
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()

            if (OwnShopState.isInOwn(context.player.uniqueId)) {
                viewFrame.open(OwnShopsListView::class.java, context.player)
            } else {
                viewFrame.open(ShopListView::class.java, context.player)
            }
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private fun createItem(context: RenderContext) = MenuHeads.CHECK.clone().apply {
        displayName {
            shopColored("Shop erstellen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                shopColored("Item: ")
                if (itemState.get(context)?.isEmpty == true) {
                    variableValue("Kein Item ausgewählt")
                } else {
                    append(
                        Component.translatable(itemState.get(context).type.translationKey())
                            .color(Colors.VARIABLE_VALUE)
                    )
                }
            }

            line {
                spacer("-")
                appendSpace()
                shopColored("Preis pro Item: ")
                if (priceState.get(context) <= 0) {
                    variableValue("Kein Preis festgelegt")
                } else {
                    variableValue(formatPriceNice(priceState.get(context)))
                }
            }
        }
    }

    private val itemNotSet = MenuHeads.QUESTION.clone().apply {
        displayName {
            shopColored("Kein Item ausgewählt")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val pricePerItemItem = MenuHeads.DOLLAR.clone().apply {
        displayName {
            shopColored("Preis pro Item festlegen")
        }
    }
}