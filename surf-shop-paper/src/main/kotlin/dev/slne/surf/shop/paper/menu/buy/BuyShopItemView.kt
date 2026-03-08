package dev.slne.surf.shop.paper.menu.buy

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.dealService
import dev.slne.surf.shop.core.util.updatedShop
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

object BuyShopItemView : View() {
    private val shopState = initialState<Shop>("buy-shop")
    private val amountState = mutableState(1)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Items kaufen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "O   I C O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I')
            .renderWith {
                shopState.get(render).item.clone().apply {
                    val oldLore = lore()?.toMutableList() ?: mutableListOf()
                    val newEntries = mutableListOf<Component>()

                    val amount = amountState.get(render)
                    val shop = shopState.get(render)

                    newEntries.add(Component.empty())
                    newEntries.add(buildText {
                        shopColored("Shopinformationen".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Einzelpreis: ")
                        variableValue(shop.pricePerItem)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Auf Lager: ")
                        if (shop.storedItemCount > 0) {
                            variableValue("${shop.storedItemCount} Items")
                        } else {
                            error("Ausverkauft")
                        }
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Verkäufer: ")
                        variableValue(shop.sellerName)
                    })

                    newEntries.add(Component.empty())

                    newEntries.add(buildText {
                        shopColored("Kaufsinformationen".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Anzahl: ")
                        variableValue(amount)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Gesamtpreis: ")
                        variableValue(formatPriceNice(amount * shop.pricePerItem))
                    })

                    lore(oldLore + newEntries)
                }
            }
            .updateOnStateChange(amountState)
            .onClick { context ->
                context.playGeneralClickSound()

                val shop = shopState.get(context)
                val amount = amountState.get(context)

                if (context.isShiftLeftClick) {
                    if (shop.storedItemCount < amount + 64) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount + 64, context)
                    return@onClick
                }

                if (context.isLeftClick) {
                    if (shop.storedItemCount < amount + 1) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount + 1, context)
                    return@onClick
                }

                if (context.isShiftRightClick) {
                    if (amount - 64 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }

                    amountState.set(amount - 64, context)
                }

                if (context.isRightClick) {
                    if (amount - 1 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount - 1, context)
                    return@onClick
                }
            }

        render.layoutSlot('C')
            .renderWith {
                MenuHeads.CHECK.clone().apply {
                    displayName {
                        shopColored("Kaufen")
                    }

                    buildLore {
                        emptyLine()
                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Klicke um die Items zu kaufen.")
                        }

                        emptyLine()
                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Anzahl: ")
                            variableValue(amountState.get(render))
                        }

                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Gesamtpreis: ")
                            variableValue(amountState.get(render) * shopState.get(render).pricePerItem)
                        }
                    }
                }
            }
            .updateOnStateChange(amountState)
            .onClick { context ->
                context.playGeneralClickSound()

                val shop = shopState.get(context).updatedShop ?: run {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop existiert nicht mehr!")
                    }

                    context.player.playNoSound()
                    context.openForPlayer(ShopListView::class.java)
                    return@onClick
                }

                val amount = amountState.get(context)

                if (shop.isBlocked) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                if (shop.storedItemCount < amount) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Es sind nicht genügend Items auf Lager!")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                context.player.closeInventory()

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logBuy(context.player, shop, amount)
                }

                plugin.launch {
                    when (val result = dealService.buy(context.player, shop, amount)) {
                        Deal.DealResult.InsufficientStock -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es sind nicht genügend Items auf Lager!")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }

                        Deal.DealResult.OtherInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (SELLER_INSUFFICIENT_FOUNDS)")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }

                        Deal.DealResult.SelfInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }

                        Deal.DealResult.ShopBlocked -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }

                        Deal.DealResult.ShopDeleted -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Dieser Shop existiert nicht mehr!")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }

                        is Deal.DealResult.Success -> {
                            context.player.sendText {
                                appendSuccessPrefix()
                                success(
                                    "Du hast erfolgreich ${result.deal.amount} Items für ${
                                        formatPriceNice(
                                            result.deal.amount * shop.pricePerItem
                                        )
                                    } gekauft!"
                                )
                            }

                            context.openForPlayer(ShopListView::class.java)
                        }

                        Deal.DealResult.TransactionFailed -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                            }

                            context.player.playNoSound()
                            context.openForPlayer(ShopListView::class.java)
                        }
                    }
                }
            }
        render.layoutSlot('B', MenuHeads.CROSS.clone().apply {
            displayName {
                error("Abbrechen")
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(ShopListView::class.java)
        }
    }
}