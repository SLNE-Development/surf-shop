package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopSaveRejectedException
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StockChangeResult
import dev.slne.surf.shop.core.common.util.logger
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.api.paper.inventory.framework.view.state.StateHandle
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificRemoveAmountPriceDialog
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import me.devnatan.inventoryframework.state.MutableState
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import java.util.logging.Level
import kotlin.math.max

val itemStorageRemoveView: AbstractSurfView = surfView("Anzahl auswählen") {
    val shopState = initialState<Shop>("edit-shop")
    val amountState = initialState<Int>("edit-amount")

    settings {
        rows(5)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(5)
    }

    onInit {
        layout(
            "         ",
            "    W    ",
            " 21 P 34 ",
            "         ",
            "Q       B"
        )
    }

    onFirstRender {
        layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            context.player.closeInventory()
            context.player.showDialog(
                createEditSpecificRemoveAmountPriceDialog(
                    shopState.get(this)
                )
            )
        }

        layoutSlot('Q', quitItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                itemStorageView::class.java,
                ImmutableMap.of("edit-shop", shopState.get(this))
            )
        }

        layoutSlot('1', minusOne).onClick { context ->
            amountState.set(this, max(0, amountState.get(this) - 1))
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('2', minusThirtyTwo).onClick { context ->
            amountState.set(this, max(0, amountState.get(this) - 64))
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('3', plusOne).onClick { context ->
            handleIncrement(this, context, 1, shopState, amountState)
        }

        layoutSlot('4', plusThirtyTwo).onClick { context ->
            handleIncrement(this, context, 64, shopState, amountState)
        }

        layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            val toRemove = amountState.get(context)

            if (toRemove <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du musst mindestens 1 Item entfernen.")
                }
                return@onClick
            }

            val shop = shopState.get(context)
            ShopService.blockShop(shop)

            plugin.launch {
                // Take the items out of the stock first (atomically, up to what is stored),
                // then hand out exactly what was taken.
                val result = try {
                    ShopService.changeStock(shop.shopUuid, -toRemove, allowPartialRemoval = true)
                } catch (e: CancellationException) {
                    ShopService.unblockShop(shop)
                    throw e
                } catch (e: ShopSaveRejectedException) {
                    logger.log(Level.WARNING, "Withdrawal of $toRemove items from shop ${shop.shopUuid} was rejected", e)
                    null
                } catch (e: Exception) {
                    logger.log(
                        Level.SEVERE,
                        "MANUAL RECONCILIATION REQUIRED: withdrawal of up to $toRemove items from shop " +
                                "${shop.shopUuid} by ${context.player.name} (${context.player.uniqueId}) has an " +
                                "unknown outcome; NO items were handed out. Verify the shop stock.",
                        e
                    )
                    ShopService.unblockShop(shop)
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Beim Entnehmen ist ein Fehler aufgetreten. Bitte melde dich beim Support.")
                    }
                    return@launch
                }

                if (result !is StockChangeResult.Changed) {
                    ShopService.unblockShop(shop)
                    context.player.sendText {
                        appendErrorPrefix()
                        error(
                            if (result == StockChangeResult.NotFound) "Der Shop existiert nicht mehr."
                            else "Die Items konnten nicht entnommen werden."
                        )
                    }
                    return@launch
                }

                val updatedShop = result.shop
                val removed = -result.appliedDelta
                val notDelivered = "MANUAL RECONCILIATION REQUIRED: $removed x ${updatedShop.item.type} were " +
                        "taken from the stock of shop ${shop.shopUuid} for ${context.player.name} " +
                        "(${context.player.uniqueId}) but may not have been handed out."

                try {
                    withContext(plugin.entityDispatcher(context.player)) {
                        val player = context.player
                        val owner = context.player.uniqueId
                        var remainingAmount = removed
                        val maxStackSize = updatedShop.item.maxStackSize

                        while (remainingAmount > 0) {
                            val giveNow = minOf(maxStackSize, remainingAmount)

                            val stack = updatedShop.item.clone()
                            stack.amount = giveNow

                            val leftover = player.inventory.addItem(stack)

                            if (leftover.isNotEmpty()) {
                                leftover.values.forEach { rest ->
                                    val dropped =
                                        player.world.dropItemNaturally(player.location, rest)
                                    dropped.owner = owner
                                }
                            }

                            remainingAmount -= giveNow
                        }

                        player.playSound(true) {
                            type(Sound.ENTITY_CHICKEN_EGG)
                        }
                    }
                } catch (e: CancellationException) {
                    logger.severe("$notDelivered Delivery was interrupted.")
                    throw e
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "$notDelivered Delivery failed.", e)
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Beim Entnehmen ist ein Fehler aufgetreten. Bitte melde dich beim Support.")
                    }
                    return@launch
                } finally {
                    ShopService.unblockShop(updatedShop)
                }

                if (plugin.auxProtectHook && removed > 0) {
                    AuxProtectHook.logWithdraw(context.player, shop, removed)
                }

                context.player.sendText {
                    appendSuccessPrefix()
                    if (removed < toRemove) {
                        success("Der Shop wurde aktualisiert. Es konnten aber nur ")
                        variableValue(removed)
                        success(" von ")
                        variableValue(toRemove)
                        success(" Items entnommen werden, da der Shop nur noch ")
                        variableValue(removed)
                        success(" Items gelagert hatte.")
                    } else {
                        success("Der Shop wurde aktualisiert und ")
                        variableValue(removed)
                        success(" Items wurden entnommen. Es sind nun noch ")
                        variableValue(updatedShop.storedItemCount)
                        success(" Items in den Shop gelagert.")
                    }
                }

                withContext(plugin.entityDispatcher(context.player)) {
                    val updatedShop = shopState.get(this@onFirstRender).updatedShop

                    if (updatedShop == null) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Der Shop existiert nicht mehr.")
                        }
                        context.player.closeInventory()
                        return@withContext
                    }

                    context.openForPlayer(
                        itemStorageView::class.java,
                        ImmutableMap.of(
                            "edit-shop",
                            updatedShop
                        )
                    )
                }
            }
        }

        layoutSlot('P').renderWith {
            valueItem(this, shopState, amountState)
        }
    }
}

private val quitItem = ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
    displayName { error("Zurück") }
}

private fun handleIncrement(
    render: RenderContext,
    context: SlotClickContext,
    delta: Int,
    shopState: StateHandle<MutableState<Shop>>,
    amountState: StateHandle<MutableState<Int>>
) {
    val currentStock = shopState.get(context)
        ?.shopUuid
        ?.let(ShopService::getShop)
        ?.storedItemCount ?: 0

    val newAmount = amountState.get(render) + delta

    if (newAmount > currentStock) {
        context.player.sendText {
            appendErrorPrefix()
            error("Es sind nicht genügend Items auf Lager!")
        }
        return
    }

    amountState.set(render, newAmount)
    context.update()

    context.player.playSound(true) {
        type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
    }
}

private fun valueItem(
    context: RenderContext,
    shopState: StateHandle<MutableState<Shop>>,
    amountState: StateHandle<MutableState<Int>>
) = buildItem(Material.GOLD_INGOT) {
    displayName {
        shopColored("Anzahl: ", TextDecoration.BOLD)
        appendSpace()
        shopColored(
            "${amountState.get(context)}/" + shopState.get(context)
                ?.shopUuid
                ?.let(ShopService::getShop)
                ?.storedItemCount
        )
    }
}

private val plusOne = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName { success("+1") }
}

private val plusThirtyTwo = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName { success("+64") }
}

private val minusOne = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
    displayName { error("-1") }
}

private val minusThirtyTwo = ViewIcon(ViewIconType.MINUS, ViewIconColor.RED).build {
    displayName { error("-64") }
}

private val continueItem = ViewIcon(ViewIconType.CHECK, ViewIconColor.GREEN).build {
    displayName { success("Auszahlen") }
}

private val ownItem = ViewIcon(ViewIconType.BELL, ViewIconColor.YELLOW).build {
    displayName { shopColored("Eigene Anzahl eingeben") }
}
