package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificRemoveAmountPriceDialog
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import kotlin.math.max

object ItemStorageRemoveView : View() {
    private val shopState = initialState<Shop>("edit-shop")
    private val amountState = initialState<Int>("edit-amount")
    private val localAmountState = mutableState(0)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Anzahl auswählen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O   W   O",
                "O21 P 34O",
                "O       O",
                "QOOOBOOOO"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        localAmountState.set(amountState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('W', ownItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()
            context.player.showDialog(
                createEditSpecificRemoveAmountPriceDialog(
                    shopState.get(render)
                )
            )
        }

        render.layoutSlot('Q', quitItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ItemStorageView::class.java,
                ImmutableMap.of("edit-shop", shopState.get(render))
            )
        }

        render.layoutSlot('1', minusOne).onClick { context ->
            localAmountState.set(max(0, localAmountState.get(render) - 1), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('2', minusThirtyTwo).onClick { context ->
            localAmountState.set(max(0, localAmountState.get(render) - 64), render)
            context.update()

            context.player.playSound(true) {
                type(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE)
            }
        }

        render.layoutSlot('3', plusOne).onClick { context ->
            handleIncrement(render, context, 1)
        }

        render.layoutSlot('4', plusThirtyTwo).onClick { context ->
            handleIncrement(render, context, 64)
        }

        render.layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()

            val toRemove = localAmountState.get(context)

            if (toRemove <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du musst mindestens 1 Item entfernen.")
                }
                return@onClick
            }

            plugin.launch {
                val shop = shopState.get(context)
                shopService.blockShop(shop)

                val updatedShop =
                    shopService.loadedShops.find { it.shopUuid == shop.shopUuid }

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

                    shopService.saveShop(updatedShop.copy(storedItemCount = 0))

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

                    shopService.saveShop(
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


                shopService.unblockShop(updatedShop)

                withContext(plugin.entityDispatcher(context.player)) {
                    val updatedShop = shopState.get(render).updatedShop

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

        render.layoutSlot('P').watch(localAmountState).renderWith {
            valueItem(render)
        }
    }

    private val quitItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName { error("Abbrechen".toSmallCaps(), TextDecoration.BOLD) }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private fun handleIncrement(render: RenderContext, context: SlotClickContext, delta: Int) {
        val currentStock = shopService.loadedShops.find {
            it.shopUuid == shopState.get(context)?.shopUuid
        }?.storedItemCount ?: 0

        val newAmount = localAmountState.get(render) + delta

        if (newAmount > currentStock) {
            context.player.sendText {
                appendErrorPrefix()
                error("Es sind nicht genügend Items auf Lager!")
            }
            return
        }

        localAmountState.set(newAmount, render)
        context.update()

        context.player.playSound(true) {
            type(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE)
        }
    }

    private fun valueItem(context: RenderContext) = buildItem(Material.GOLD_INGOT) {
        displayName {
            shopColored("Anzahl: ", TextDecoration.BOLD)
            appendSpace()
            shopColored(("${localAmountState.get(context)}/" + shopService.loadedShops.find {
                it.shopUuid == shopState.get(
                    context
                )?.shopUuid
            }?.storedItemCount))
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
}
