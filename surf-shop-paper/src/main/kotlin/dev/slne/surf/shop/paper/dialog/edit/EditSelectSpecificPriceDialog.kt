package dev.slne.surf.shop.paper.dialog.edit

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin

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
                error("Bitte beachte, das der Preis nicht kleiner als 0.01 sein darf. Beachte außerdem, dass der Preis pro Item gilt, nicht für den gesamten Stapel.")
            }

            input {
                text("price") {
                    label { shopColored("Preis pro Item") }
                    width(300)
                    initial(shop.pricePerItem.toString())
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
                            EditShopView::class.java, player, ImmutableMap.of(
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
                        val price = response.getText("price")?.trim()?.toDoubleOrNull() ?: 0.0

                        if (price < 0.01) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Preis muss mindestens 0.01 CC betragen.")
                            }
                            return@customPlayerClick
                        }

                        plugin.launch(plugin.entityDispatcher(player)) {
                            player.closeDialog()

                            viewFrame.open(
                                EditShopView::class.java, player, ImmutableMap.of(
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