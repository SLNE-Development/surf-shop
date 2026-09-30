package dev.slne.surf.shop.paper.menu.buy

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.displayKey
import dev.slne.surf.shop.paper.util.formatPriceNice
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit


val buyShopItemView: AbstractSurfView = surfView("Items Kaufen") {
    val shopState = initialState<Shop>("buy-shop")
    val amountState = mutableState(1)

    settings {
        rows(3)
        cancelAllInteractions()
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(2, exemptColumns = intArrayOf(3, 4, 5, 6, 7))
        blockRow(3)
    }

    onInit {
        layout("         ", "    I C  ", "B        ")
    }

    onFirstRender {
        layoutSlot('I')
            .renderWith {
                val initialShop = shopState[this@onFirstRender]
                val shop = initialShop.updatedShop ?: initialShop
                val amount = amountState[this@onFirstRender]

                shop.item.clone().apply {
                    val oldLore = lore()?.toMutableList() ?: mutableListOf()
                    val newEntries = mutableListOf<Component>()

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

                    newEntries.add(Component.empty())
                    newEntries.add(buildText {
                        shopColored("Anzahl".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.left")
                        darkSpacer(":")
                        variableValue(" +1".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.left")
                        spacer(" + ")
                        white("Shift")
                        darkSpacer(":")
                        variableValue(" +64".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.right")
                        darkSpacer(":")
                        variableValue(" -1".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.right")
                        spacer(" + ")
                        white("Shift")
                        darkSpacer(":")
                        variableValue(" -64".toSmallCaps())
                    })

                    lore(oldLore + newEntries)
                }
            }
            .onClick { context ->
                context.playGeneralClickSound()

                if (!context.player.canUseShopTransactionsFromCurrentView()) {
                    context.player.sendText {
                        appendInfoPrefix()
                        info("Dieser Shop ist hier nur zur Ansicht. Zum Kaufen musst du zum Spawn.")
                    }
                    context.player.playNoSound()
                    return@onClick
                }

                val currentShop = shopState[context].updatedShop ?: run {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop existiert nicht mehr!")
                    }

                    context.player.playNoSound()
                    if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                        StaticShopState.setInStaticShop(context.player.uniqueId, false)
                        context.player.closeInventory()
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(ownShopsListView::class.java)
                    } else {
                        context.openForPlayer(shopListView::class.java)
                    }
                    return@onClick
                }

                val stock = currentShop.storedItemCount

                if (stock <= 0) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop ist ausverkauft!")
                    }
                    context.player.playNoSound()
                    context.update()
                    return@onClick
                }

                val delta = when {
                    context.isShiftLeftClick -> 64
                    context.isShiftRightClick -> -64
                    context.isLeftClick -> 1
                    context.isRightClick -> -1
                    else -> return@onClick
                }

                val current = amountState[context].coerceAtMost(stock)
                val newAmount = current + delta

                when {
                    newAmount < 1 -> {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }
                        context.player.playNoSound()
                        amountState[context] = current
                    }

                    newAmount > stock -> {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager! (Verfügbar: $stock)")
                        }
                        context.player.playNoSound()
                        amountState[context] = current
                    }

                    else -> amountState[context] = newAmount
                }

                context.update()
            }

        this.layoutSlot('C')
            .renderWith {
                ViewIcon(ViewIconType.CHECK, ViewIconColor.GREEN).build {
                    displayName {
                        success("Kaufen")
                    }

                    buildLore {
                        emptyLine()
                        line {
                            appendBlob()
                            spacer(
                                "Klicke um die Items zu kaufen.".toSmallCaps(),
                                TextDecoration.BOLD
                            )
                        }

                        emptyLine()
                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Anzahl: ")
                            variableValue(amountState[this@onFirstRender])
                        }

                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Gesamtpreis: ")
                            variableValue(
                                amountState[this@onFirstRender] * shopState[this@onFirstRender].pricePerItem
                            )
                        }
                    }
                }
            }
            .onClick { context ->
                context.playGeneralClickSound()

                if (!context.player.canUseShopTransactionsFromCurrentView()) {
                    context.player.sendText {
                        appendInfoPrefix()
                        info("Dieser Shop ist hier nur zur Ansicht. Zum Kaufen musst du zum Spawn.")
                    }
                    context.player.playNoSound()
                    return@onClick
                }

                val shop = shopState[context].updatedShop ?: run {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop existiert nicht mehr!")
                    }

                    context.player.playNoSound()
                    if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                        StaticShopState.setInStaticShop(context.player.uniqueId, false)
                        context.player.closeInventory()
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(ownShopsListView::class.java)
                    } else {
                        context.openForPlayer(shopListView::class.java)
                    }
                    return@onClick
                }

                val amount = amountState[context]

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
                    when (val result = DealService.buy(context.player.uniqueId, shop, amount)) {
                        Deal.DealResult.InsufficientStock -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es sind nicht genügend Items auf Lager!")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.OtherInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.SelfInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.ShopBlocked -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.ShopDeleted -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Dieser Shop existiert nicht mehr!")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
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

                            Bukkit.getPlayer(shop.seller)?.sendText {
                                appendInfoPrefix()
                                variableValue(context.player.name)
                                info(" hat gerade ")
                                append {
                                    if (result.deal.amount > 1) {
                                        variableValue("${amount}x ")
                                    }
                                    append(shop.item.displayName())
                                    hoverEvent(shop.item.asHoverEvent())
                                }
                                info(" gekauft.")
                                spacer(" (${formatPriceNice(shop.pricePerItem * result.deal.amount)} - 3% Steuern)")
                            }

                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.TransactionFailed -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }

                        Deal.DealResult.PlayerNotFound -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
                            }

                            context.player.playNoSound()
                            openListView(this@onFirstRender)
                        }
                    }
                }
            }
        this.layoutSlot('B', ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
            displayName {
                error("Abbrechen")
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                StaticShopState.setInStaticShop(context.player.uniqueId, false)
                context.player.closeInventory()
            } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                context.openForPlayer(ownShopsListView::class.java)
            } else {
                context.openForPlayer(shopListView::class.java)
            }
        }
    }
}

private suspend fun openListView(context: RenderContext) =
    withContext(plugin.entityDispatcher(context.player)) {
        if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
            StaticShopState.setInStaticShop(context.player.uniqueId, false)
            context.player.closeInventory()
        } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
            viewFrame.open(ownShopsListView::class.java, context.player)
        } else {
            viewFrame.open(shopListView::class.java, context.player)
        }
    }
