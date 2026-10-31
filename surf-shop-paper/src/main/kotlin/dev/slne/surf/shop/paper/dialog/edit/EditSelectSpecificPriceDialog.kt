package dev.slne.surf.shop.paper.dialog.edit

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.sendInvalidPriceMessage

@Suppress("UnstableApiUsage")
fun createEditSpecificPriceDialog(
    shop: Shop
) = dialog {
    base {
        title { shopColored("Item Preis festgelegen") }
        body {
            plainMessage {
                info("Hier kannst du einen Preis für das Item festlegen, welches verkaufst.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, dass der Preis zwischen 0,01 und 100.000.000 CC liegen muss und höchstens 2 Nachkommastellen haben darf. Beachte außerdem, dass der Preis pro Item gilt, nicht für den gesamten Stapel.")
            }

            input {
                text("price") {
                    label { shopColored("Preis pro Item") }
                    width(300)
                    initial(ShopPriceRules.formatPriceInput(shop.pricePerItem))
                    maxLength(64)
                }
            }
        }

        type {
            confirmation(actionButton {
                label { error("Abbrechen") }
                tooltip { info("Klicke, um zurück zu gelangen.") }
                width(200)

                action {
                    customPlayerClick { _, player ->
                        player.closeDialog()
                        viewFrame.open(
                            editShopView::class.java, player, ImmutableMap.of(
                                "edit-shop", shop,
                            )
                        )
                    }
                }
            }, actionButton {
                label { success("Bestätigen") }
                tooltip { info("Klicke, um den Preis zu bestätigen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val price = ShopPriceRules.parsePriceInput(response.getText("price"))

                        if (price == null) {
                            player.sendInvalidPriceMessage()
                            return@customPlayerClick
                        }

                        plugin.launch(plugin.entityDispatcher(player)) {
                            player.closeDialog()

                            viewFrame.open(
                                editShopView::class.java, player, ImmutableMap.of(
                                    "edit-shop", shop.copy(pricePerItem = price)
                                )
                            )
                        }
                    }
                }
            })
        }
    }
}