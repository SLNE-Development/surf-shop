package dev.slne.surf.shop.paper.dialog.create

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.paper.menu.CreateShopView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
fun createSpecificPriceDialog(
    itemStack: ItemStack,
    initialPrice: Int
) = dialog {
    base {
        title { shopColored("Item Preis auswählen") }
        body {
            plainMessage {
                info("Hier kannst du einen Preis für das Item festlegen, welches du verkaufen möchtest.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das der Preis nicht kleiner als 1 sein darf. Beachte außerdem, dass der Preis pro Item gilt, nicht für den gesamten Stapel.")
            }

            input {
                text("price") {
                    label { shopColored("Preis pro Item") }
                    width(300)
                    initial(initialPrice.toString())
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
                            CreateShopView::class.java, player, ImmutableMap.of(
                                "create-item", itemStack,
                                "create-price", initialPrice
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
                            CreateShopView::class.java, player, ImmutableMap.of(
                                "create-item", itemStack,
                                "create-price", price
                            )
                        )
                    }
                }
            })
        }
    }
}