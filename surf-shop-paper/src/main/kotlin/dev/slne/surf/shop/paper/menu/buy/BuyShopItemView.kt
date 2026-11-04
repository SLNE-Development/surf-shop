package dev.slne.surf.shop.paper.menu.buy

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
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.runOnOwningThread
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.displayKey
import dev.slne.surf.shop.paper.util.formatPriceNice
import kotlinx.coroutines.CancellationException
import java.util.logging.Level
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player


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
        blockRow(2)
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
                        variableValue(formatPriceNice(ShopPriceRules.totalPrice(shop.pricePerItem, amount)))
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
                                formatPriceNice(
                                    ShopPriceRules.totalPrice(
                                        shopState[this@onFirstRender].pricePerItem,
                                        amountState[this@onFirstRender]
                                    )
                                )
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

                // The shop this view was opened with is the quote; refuse if the price changed since.
                val quotedShop = shopState[context]
                if (quotedShop.pricePerItem != shop.pricePerItem) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Der Preis dieses Shops hat sich geändert. Bitte prüfe den neuen Preis.")
                    }

                    context.player.playNoSound()
                    openListView(this@onFirstRender)
                    return@onClick
                }

                if (!ShopPriceRules.isAllowedTotal(ShopPriceRules.totalPrice(shop.pricePerItem, amount))) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Der Gesamtpreis ist zu hoch. Bitte kaufe weniger Items.")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                context.player.closeInventory()


                plugin.launch {
                    val player = context.player
                    val result = try {
                        DealService.buy(player.uniqueId, quotedShop, amount)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.log(
                            Level.SEVERE,
                            "Purchase by ${player.uniqueId} in shop ${shop.shopUuid} failed unexpectedly",
                            e
                        )
                        Deal.DealResult.TransactionFailed
                    }

                    player.sendBuyResult(result, quotedShop)
                    if (result is Deal.DealResult.Success) notifySeller(player.name, quotedShop, result.deal.amount)

                    player.runOnOwningThread(plugin) {
                        if (result !is Deal.DealResult.Success) player.playNoSound()
                        openListView(this@onFirstRender)
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

/** Returns the player to the shop list they came from. Must run on the player's thread. */
private fun openListView(context: RenderContext) {
    if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
        StaticShopState.setInStaticShop(context.player.uniqueId, false)
        context.player.closeInventory()
    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
        viewFrame.open(ownShopsListView::class.java, context.player)
    } else {
        viewFrame.open(shopListView::class.java, context.player)
    }
}

private fun Player.sendBuyResult(result: Deal.DealResult, quotedShop: Shop) = sendText {
    fun failure(message: String) {
        appendErrorPrefix()
        error(message)
    }

    when (result) {
        is Deal.DealResult.Success -> {
            appendSuccessPrefix()
            success(
                "Du hast erfolgreich ${result.deal.amount} Items für ${
                    formatPriceNice(ShopPriceRules.totalPrice(quotedShop.pricePerItem, result.deal.amount))
                } gekauft!"
            )
        }

        Deal.DealResult.InsufficientStock -> failure("Es sind nicht genügend Items auf Lager!")
        Deal.DealResult.SelfInsufficientFounds,
        Deal.DealResult.OtherInsufficientFounds -> failure("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")

        Deal.DealResult.ShopBlocked -> failure("Du kannst derzeit keine Items in diesem Shop kaufen!")
        Deal.DealResult.ShopDeleted -> failure("Dieser Shop existiert nicht mehr!")
        Deal.DealResult.TransactionFailed -> failure(
            "Der Kauf konnte nicht abgeschlossen werden. Falls dir Geld abgebucht wurde, melde dich bitte beim Support. (TRANSACTION_FAILED)"
        )

        Deal.DealResult.PlayerNotFound -> failure("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
    }
}

private fun notifySeller(buyerName: String, shop: Shop, amount: Int) {
    Bukkit.getPlayer(shop.seller)?.sendText {
        appendInfoPrefix()
        variableValue(buyerName)
        info(" hat gerade ")
        append {
            if (amount > 1) {
                variableValue("${amount}x ")
            }
            append(shop.item.displayName())
            hoverEvent(shop.item.asHoverEvent())
        }
        info(" gekauft.")
        spacer(" (${formatPriceNice(ShopPriceRules.totalPrice(shop.pricePerItem, amount))} - 3% Steuern)")
    }
}
