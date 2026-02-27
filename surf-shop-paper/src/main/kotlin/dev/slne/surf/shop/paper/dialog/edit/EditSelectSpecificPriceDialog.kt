package dev.slne.surf.shop.paper.dialog.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline

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
                error("Bitte beachte, das der Preis nicht kleiner als 1 sein darf. Beachte außerdem, dass der Preis pro Item gilt, nicht für den gesamten Stapel.")
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
                        val price = response.getText("price")?.trim()?.toIntOrNull() ?: 0

                        player.closeDialog()

                        viewFrame.open(
                            EditShopView::class.java, player, ImmutableMap.of(
                                "edit-shop", shop.copy(pricePerItem = price)
                            )
                        )
                    }
                }
            })
        }
    }
}