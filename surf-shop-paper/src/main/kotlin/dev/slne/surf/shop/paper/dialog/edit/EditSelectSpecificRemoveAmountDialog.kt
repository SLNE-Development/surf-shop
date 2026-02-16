package dev.slne.surf.shop.paper.dialog.edit

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageRemoveView
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline

@Suppress("UnstableApiUsage")
fun createEditSpecificRemoveAmountPriceDialog(
    auction: Auction
) = dialog {
    base {
        title { auctionColored("Anzahl auswählen") }
        body {
            plainMessage {
                info("Hier kannst du die Anzahl angeben, die du an Items auszahlen möchtest. Diese Anzahl wird von der Gesamtanzahl der Auktion abgezogen.")
                appendNewline(2)
                appendWarningPrefix()
                error("Bitte beachte, das die Anzahl nicht kleiner als 1 sein darf. Diese Anzahl wird von der Gesamtanzahl der Auktion abgezogen.")
            }

            input {
                text("amount") {
                    label { auctionColored("Anzahl an Items") }
                    width(300)
                    initial(if (auction.storedItemCount > 64) "64" else auction.storedItemCount.toString())
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
                                "edit-auction", auction,
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
                                "edit-auction", auction,
                                "edit-amount", amount
                            )
                        )
                    }
                }
            })
        }
    }
}