package dev.slne.surf.shop.paper.dialog.create

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import dev.slne.surf.shop.paper.menu.createShopView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.sendInvalidPriceMessage
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
fun createSpecificPriceDialog(
    itemStack: ItemStack,
    initialPrice: Double
) = dialog {
    base {
        title { shopColored("Item Preis auswählen") }
        body {
            plainMessage {
                info("Hier kannst du einen Preis für das Item festlegen, welches du verkaufen möchtest.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, dass der Preis zwischen 0,01 und 100.000.000 CC liegen muss und höchstens 2 Nachkommastellen haben darf. Beachte außerdem, dass der Preis pro Item gilt, nicht für den gesamten Stapel.")
            }

            input {
                text("price") {
                    label { shopColored("Preis pro Item") }
                    width(300)
                    initial(ShopPriceRules.formatPriceInput(initialPrice))
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
                            createShopView::class.java, player, ImmutableMap.of(
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
                        val price = ShopPriceRules.parsePriceInput(response.getText("price"))

                        if (price == null) {
                            player.sendInvalidPriceMessage()
                            return@customPlayerClick
                        }

                        plugin.launch(plugin.entityDispatcher(player)) {
                            player.closeDialog()

                            viewFrame.open(
                                createShopView::class.java, player, ImmutableMap.of(
                                    "create-item", itemStack,
                                    "create-price", price
                                )
                            )
                        }
                    }
                }
            })
        }
    }
}