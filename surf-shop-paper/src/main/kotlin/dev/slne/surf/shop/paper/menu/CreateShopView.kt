package dev.slne.surf.shop.paper.menu

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.onInit
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.surfView
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.util.base64
import dev.slne.surf.shop.paper.chest.ShopChestSetupView
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.hook.FancyHologramsHook
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import dev.slne.surf.shop.paper.util.location
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

val createShopView = surfView("Shop erstellen") {
    val itemState = initialState<ItemStack>("create-item")
    val priceState = initialState<Double>("create-price")

    settings {
        rows(5)
    }

    onInit {
        layout("OOOOOOOOO", "O       O", "O P I C O", "O       O", "OOOOBOOOO")
    }

    onFirstRender {
        layoutSlot('P', pricePerItemItem.clone().apply {
            if (priceState[this@onFirstRender] > 0) {
                buildLore {
                    emptyLine()
                    line {
                        spacer("-")
                        appendSpace()
                        shopColored("Aktueller Preis Pro Item: ${priceState[this@onFirstRender]}")
                    }
                }
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                PriceSelectView::class.java,
                ImmutableMap.of(
                    "create-item", itemState[this@onFirstRender],
                    "create-price", priceState[this@onFirstRender]
                )
            )
        }

        if (itemState[this].isEmpty) {
            layoutSlot('I', itemNotSet).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState[context],
                        "create-item",
                        ItemStack.empty()
                    )
                )
            }
        } else {
            layoutSlot('I', itemState[this].clone().apply {
                amount = 1
            }).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState[context],
                        "create-item",
                        ItemStack.empty()
                    )
                )
            }
        }

        layoutSlot('C', createItem(itemState[this], priceState[this])).onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canCreateShopFromCurrentView()) {
                context.player.sendText {
                    appendInfoPrefix()
                    info("Shops kannst du nur am Spawn erstellen.")
                }
                context.player.playNoSound()
                return@onClick
            }

            val item = itemState[context]
            val price = priceState[context]

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
                val shop = ShopService.createShop(item.clone().apply {
                    amount = 1
                }.base64, 0, price, context.player.uniqueId)

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logCreate(context.player, shop)
                }

                val chest = ChestShopEditState.getChest(context.player.uniqueId)
                if (chest != null) {
                    val updatedChest =
                        StaticShopChestService.updateChestShop(chest.chestUuid, shop.shopUuid)
                    if (updatedChest != null) {
                        ChestShopEditState.setChest(context.player.uniqueId, updatedChest)
                    }

                    if (plugin.hasFancyHolograms) {
                        chest.location?.let {
                            FancyHologramsHook.createAndOrDelete(chest.chestUuid, it, shop)
                        }
                    }
                }

                context.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Der Shop wurde erstellt!")
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    context.player.closeInventory()
                    viewFrame.open(
                        editShopView::class.java,
                        context.player,
                        ImmutableMap.of("edit-shop", shop)
                    )
                }
            }
        }
        layoutSlot('B', backItem).onClick { context ->
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
                viewFrame.open(shopListView::class.java, context.player)
            }
        }
    }
}

private fun createItem(item: ItemStack, price: Double) = MenuHeads.CHECK.clone().apply {
    displayName {
        shopColored("Shop erstellen")
    }

    buildLore {
        emptyLine()
        line {
            spacer("-")
            appendSpace()
            shopColored("Item: ")
            if (item?.isEmpty == true) {
                variableValue("Kein Item ausgewählt")
            } else {
                append(
                    Component.translatable(item.type.translationKey())
                        .color(Colors.VARIABLE_VALUE)
                )
            }
        }

        line {
            spacer("-")
            appendSpace()
            shopColored("Preis pro Item: ")
            if (price <= 0) {
                variableValue("Kein Preis festgelegt")
            } else {
                variableValue(formatPriceNice(price))
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
