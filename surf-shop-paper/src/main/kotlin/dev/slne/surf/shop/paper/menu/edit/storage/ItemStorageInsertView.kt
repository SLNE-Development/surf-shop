package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
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
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopSaveRejectedException
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StockChangeResult
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import java.util.logging.Level

val itemStorageInsertView: AbstractSurfView = surfView("Items einlagern") {
    val shopState = initialState<Shop>("edit-shop")
    val itemInsertedState = mutableState(0)

    settings {
        rows(5)
        cancelOnClick()
        cancelOnDrag()
        cancelOnDrop()
        cancelOnPickup(false)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(5)
    }

    onInit {
        layout(
            "         ",
            " SSSSSSS ",
            " SSSSSSS ",
            " SSSSSSS ",
            "B        ",
        )
    }

    onFirstRender {
        layoutSlot('S', ItemStack.empty()).onClick { context ->
            context.isCancelled = false
        }

        slot(1, 5).renderWith {
            explainItem(shopState[this].storedItemCount)
        }

        layoutSlot('B', ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
            displayName { error("Zurück") }
        }).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(itemStorageView::class.java, mapOf("edit-shop" to shopState[this]))
        }
    }

    onClick {
        if (!this.clickedContainer.isEntityContainer) {
            return@onClick
        }

        if (!this.player.canEditShopStorageFromCurrentView()) {
            this.player.sendText {
                appendErrorPrefix()
                error("Das Lager kannst du nur am Spawn bearbeiten.")
            }
            this.player.playNoSound()
            return@onClick
        }

        val item = this.item ?: return@onClick
        val shop = shopState[this]

        if (!item.isSimilar(shop.item)) {
            return@onClick
        }

        this.isCancelled = false
        this.clickOrigin.currentItem = ItemStack.empty()

        plugin.launch {
            val player = this@onClick.player
            val amount = item.amount
            val shopUuid = shopState[this@onClick].shopUuid

            var outcomeUnknown = false
            val result = try {
                ShopService.changeStock(shopUuid, amount)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ShopSaveRejectedException) {
                logger.log(Level.WARNING, "Deposit of $amount items into shop $shopUuid was rejected", e)
                null
            } catch (e: Exception) {
                outcomeUnknown = true
                logger.log(
                    Level.SEVERE,
                    "MANUAL RECONCILIATION REQUIRED: deposit of $amount x ${item.type} by ${player.name} " +
                            "(${player.uniqueId}) into shop $shopUuid has an unknown outcome; the items were NOT " +
                            "returned. Verify the shop stock and refund the player if the deposit is missing.",
                    e
                )
                null
            }

            if (outcomeUnknown) {
                player.sendText {
                    appendErrorPrefix()
                    error("Beim Einlagern ist ein Fehler aufgetreten. Bitte melde dich beim Support.")
                }
                player.playNoSound()
                return@launch
            }

            if (result !is StockChangeResult.Changed) {
                withContext(plugin.entityDispatcher(player)) {
                    // An offline player's block runs on the calling thread; the item cannot be returned.
                    if (!player.isOnline) {
                        logger.severe(
                            "MANUAL RECONCILIATION REQUIRED: deposit of $amount x ${item.type} into shop $shopUuid " +
                                    "failed and ${player.name} (${player.uniqueId}) went offline before the item " +
                                    "could be returned. Give the item back."
                        )
                        return@withContext
                    }

                    player.inventory.addItem(item).values.forEach {
                        player.world.dropItemNaturally(player.location, it).owner = player.uniqueId
                    }
                }

                player.sendText {
                    appendErrorPrefix()
                    error(
                        when (result) {
                            StockChangeResult.Overflow -> "Das Lager dieses Shops ist voll."
                            StockChangeResult.NotFound -> "Der Shop existiert nicht mehr."
                            else -> "Die Items konnten nicht eingelagert werden."
                        }
                    )
                }
                player.playNoSound()
                return@launch
            }

            shopState[this@onClick] = result.shop
            itemInsertedState[this@onClick] = itemInsertedState[this@onClick] + amount

            if (plugin.auxProtectHook) {
                AuxProtectHook.logDeposit(player, result.shop, amount)
            }

            this@onClick.player.sendActionBar(buildText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("$amount Items")
                success(" eingelagert.")
            })

            this@onClick.player.playSound(true) {
                type(Sound.ENTITY_PLAYER_LEVELUP)
            }
        }
    }

    onClose {
        val added = itemInsertedState[this]
        if (added > 0) {
            this.player.sendText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("${added}x ")
                translatable(shopState[this@onClose].item.type.translationKey())
                success(" eingelagert.")
            }

            this.player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_YES)
            }
        }
    }
}

private fun explainItem(amount: Int) = MenuHeads.QUESTION.clone().apply {
    displayName {
        shopColored("Erklärung".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        emptyLine()
        line {
            appendBlob()
            shopColored("Klicke auf ein Item, um es einzulagern.")
        }
        line {
            appendBlob()
            shopColored("Das ausgewählte Item wird sofort in deinen Shop eingelagert")
        }
        line {
            appendSpace()
            appendSpace()
            appendSpace()
            shopColored("und steht zum Verkauf bereit.")
        }
        emptyLine()
        line {
            spacer("Derzeit sind ")
            variableValue("$amount Items")
            spacer(" im Lager.")
        }
    }
}
