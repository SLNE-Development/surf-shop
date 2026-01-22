package dev.slne.surf.shop.auction.paper.server.gui

import com.github.shynixn.mccoroutine.folia.launch
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons
import dev.slne.surf.shop.auction.api.auction.AuctionManager
import dev.slne.surf.shop.auction.paper.api.auction.itemStack
import dev.slne.surf.shop.auction.paper.server.plugin
import dev.slne.surf.surfapi.bukkit.api.builder.ItemStack
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.playerMenu
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.slot
import dev.slne.surf.surfapi.bukkit.api.inventory.dsl.staticPane
import dev.slne.surf.surfapi.bukkit.api.inventory.types.SurfChestSinglePlayerGui
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.format.DateTimeFormatter

object AuctionGui {
    fun mainGui(
        player: Player
    ) = playerMenu(text("Auktionen"), player, 6) {
        // Categories
        staticPane(slot(0, 0), 6, 1) {
            fillWith(fillerItem)
        }

        // Filters and Settings
        staticPane(slot(0, 5), 1, 9) {
            fillWith(fillerItem)

            item(slot(1, 0), sortItem) {
                click = { event ->

                }
            }

            item(slot(2, 0), filterItem) {
                click = { event ->

                }
            }
        }

        addAuctionItems()
    }

    private val backButton = GuiItem(ItemStack(Material.CYAN_STAINED_GLASS_PANE) {
        displayName {
            primary("Zurück")
        }
    })

    private val nextButton = GuiItem(ItemStack(Material.LIME_STAINED_GLASS_PANE) {
        displayName {
            primary("Weiter")
        }
    })

    private fun SurfChestSinglePlayerGui.addAuctionItems() {
        val pagination = PaginatedPane(slot(1, 0), 8, 5)

        val pagingButtons = PagingButtons(slot(7, 5), 2, Pane.Priority.HIGHEST, pagination).apply {
            setBackwardButton(backButton)
            setForwardButton(nextButton)
        }

        plugin.launch {
            pagination.populateWithGuiItems(buildAuctionItems())
            update()
        }

        addPane(pagination)
        addPane(pagingButtons)
    }

    suspend fun buildAuctionItems() = AuctionManager.auctions.map { auction ->
        val itemStack = auction.itemStack.clone()
        val newItemStack = ItemStack(itemStack.type, itemStack.amount) {
            itemStack.enchantments.forEach { (enchantment, level) ->
                addUnsafeEnchantment(enchantment, level)
            }

            val owner = auction.owner()

            buildLore {
                emptyLine()

                line {
                    variableKey("Auktion #")
                    variableValue(auction.uuid.toString())
                }

                line {
                    variableKey("Verkäufer: ")
                    variableValue(owner.lastKnownName ?: owner.uuid.toString())
                }

                line {
                    variableKey("Startgebot: ")
                    variableValue(auction.startingBid)
                }

                line {
                    variableKey("Aktuelles Gebot: ")
                    variableValue(auction.currentBid?.amount ?: auction.startingBid)
                }

                val instantBuyPrice = auction.instantBuyPrice
                if (auction.instantBuyEnabled && instantBuyPrice != null) {
                    line {
                        variableKey("Sofortkauf: ")
                        variableValue(instantBuyPrice)
                    }
                }

                line {
                    variableKey("Auktionsende: ")
                    variableValue(auction.endsAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                }
            }
        }

        GuiItem(newItemStack) { event ->
            event.whoClicked.sendText {
                primary("You clicked on auction #${auction.uuid}")
            }
        }
    }

    private val fillerItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            primary(" ")
        }
    }

    private val sortItem = ItemStack(Material.HOPPER) {
        displayName {
            primary("Sortieren")
        }

        buildLore {
            line {
                spacer("Sort Items")
            }
        }
    }

    private val filterItem = ItemStack(Material.NAME_TAG) {
        displayName {
            primary("Filter")
        }

        buildLore {
            line {
                spacer("Filter Items")
            }
        }
    }
}