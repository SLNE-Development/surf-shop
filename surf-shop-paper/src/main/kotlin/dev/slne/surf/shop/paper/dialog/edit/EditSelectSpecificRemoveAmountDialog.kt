package dev.slne.surf.shop.paper.dialog.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageRemoveView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline

@Suppress("UnstableApiUsage")
fun createEditSpecificRemoveAmountPriceDialog(
    shop: Shop
) = dialog {
    base {
        title { shopColored("Anzahl auswählen") }
        body {
            plainMessage {
                info("Hier kannst du die Anzahl angeben, die du an Items auszahlen möchtest.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das die Anzahl nicht kleiner als 1 sein darf.")
            }

            input {
                text("amount") {
                    label { shopColored("Anzahl an Items") }
                    width(300)
                    initial(if (shop.storedItemCount > 64) "64" else shop.storedItemCount.toString())
                    maxLength(5)
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
                            ItemStorageRemoveView::class.java, player, ImmutableMap.of(
                                "edit-shop", shop,
                                "edit-amount", 0
                            )
                        )
                    }
                }
            }, actionButton {
                label { success("Bestätigen") }
                tooltip { info("Klicke, um die Anzahl zu bestätigen.") }
                width(200)

                action {
                    customPlayerClick { response, player ->
                        val amount = response.getText("amount")?.trim()?.toIntOrNull() ?: 0

                        player.closeDialog()

                        viewFrame.open(
                            ItemStorageRemoveView::class.java, player, ImmutableMap.of(
                                "edit-shop", shop,
                                "edit-amount", amount
                            )
                        )
                    }
                }
            })
        }
    }
}