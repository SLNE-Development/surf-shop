package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockColumn
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.paper.chest.ShopChestSelectShopView.initialState
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificRemoveAmountPriceDialog
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import me.devnatan.inventoryframework.state.MutableState
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

val itemStorageRemoveView = surfView("Anzahl auswählen") {
    val shopState = initialState<Shop>("edit-shop")
    val amountState = initialState<Int>("edit-amount")

    settings {
        rows(5)
    }

    containerDefaults {
        blockRow(1)
        blockRow(5)
        blockColumn(0)
        blockColumn(8)
    }

    onInit {
        layout(
            "OOOOOOOOO",
            "O   W   O",
            "O21 P 34O",
            "O       O",
            "QOOOBOOOO"
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
                ItemStorageView::class.java,
                ImmutableMap.of("edit-shop", shopState.get(this))
            )
        }

        layoutSlot('1', minusOne).onClick { context ->
            amountState.set(max(0, amountState.get(this) - 1), this)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        layoutSlot('2', minusThirtyTwo).onClick { context ->
            amountState.set(max(0, amountState.get(this) - 64), this)
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
                val updatedShop =
                    ShopService.getShop(shop.shopUuid)

                if (updatedShop == null) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Der Shop existiert nicht mehr.")
                    }
                    return@launch
                }

                if (updatedShop.storedItemCount < toRemove) {
                    val amountToGive = updatedShop.storedItemCount

                    withContext(plugin.entityDispatcher(context.player)) {
                        val player = context.player
                        val owner = context.player.uniqueId
                        var remainingAmount = amountToGive
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

                    ShopService.saveShop(updatedShop.copy(storedItemCount = 0))

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Der Shop wurde aktualisiert. Es konnten aber nur ")
                        variableValue(amountToGive)
                        success(" von ")
                        variableValue(toRemove)
                        success(" Items entnommen werden, da der Shop nur noch ")
                        variableValue(amountToGive)
                        success(" Items gelagert hatte.")
                    }
                } else {
                    withContext(plugin.entityDispatcher(context.player)) {
                        val player = context.player
                        val owner = context.player.uniqueId
                        var remainingAmount = toRemove
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

                    ShopService.saveShop(
                        updatedShop.copy(
                            storedItemCount = updatedShop.storedItemCount - toRemove
                        )
                    )

                    if (plugin.auxProtectHook) {
                        AuxProtectHook.logWithdraw(context.player, shop, toRemove)
                    }

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Der Shop wurde aktualisiert und ")
                        variableValue(toRemove)
                        success(" Items wurden entnommen. Es sind nun noch ")
                        variableValue(updatedShop.storedItemCount - toRemove)
                        success(" Items in den Shop gelagert.")
                    }
                }


                ShopService.unblockShop(updatedShop)

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
                        ItemStorageView::class.java,
                        ImmutableMap.of(
                            "edit-shop",
                            updatedShop
                        )
                    )
                }
            }
        }

        layoutSlot('P').watch(amountState).renderWith {
            valueItem(this, shopState, amountState)
        }
    }
}

private val quitItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
    displayName { error("Abbrechen".toSmallCaps(), TextDecoration.BOLD) }
}

private fun handleIncrement(
    render: RenderContext,
    context: SlotClickContext,
    delta: Int,
    shopState: MutableState<Shop>,
    amountState: MutableState<Int>
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

    amountState.set(newAmount, render)
    context.update()

    context.player.playSound(true) {
        type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
    }
}

private fun valueItem(
    context: RenderContext,
    shopState: MutableState<Shop>,
    amountState: MutableState<Int>
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

private val plusOne = MenuHeads.PLUS.clone().apply {
    displayName { shopColored("+1") }
}

private val plusThirtyTwo = MenuHeads.PLUS.clone().apply {
    displayName { shopColored("+64") }
}

private val minusOne = MenuHeads.MINUS.clone().apply {
    displayName { shopColored("-1") }
}

private val minusThirtyTwo = MenuHeads.MINUS.clone().apply {
    displayName { shopColored("-64") }
}

private val continueItem = MenuHeads.CHECK.clone().apply {
    displayName { shopColored("Auszahlen") }
}

private val ownItem = MenuHeads.DOLLAR.clone().apply {
    displayName { shopColored("Eigene Anzahl eingeben") }
}
