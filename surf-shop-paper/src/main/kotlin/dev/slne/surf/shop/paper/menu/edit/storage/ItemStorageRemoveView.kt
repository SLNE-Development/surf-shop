package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.paper.dialog.edit.createEditSpecificRemoveAmountPriceDialog
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
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
                "QOOOBOOOU"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        localAmountState.set(amountState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('W', ownItem).onClick { context ->
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

        render.layoutSlot('U', shulkerSlotItem).onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canUseShulkerFeature()) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Dir fehlt die Berechtigung, um Shulker-Boxen im Shop zu nutzen.")
                })
                context.player.playNoSound()
                return@onClick
            }

            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            val shop = shopState.get(context)

            if (Tag.SHULKER_BOXES.isTagged(shop.item.type)) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Du kannst keine Shulker-Boxen in einer Shulker-Box lagern.")
                })
                context.player.playNoSound()
                return@onClick
            }

            val shulker = context.player.findFillableShulker(shop.item)?.asQuantity(1) ?: run {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Du brauchst eine Shulker-Box mit freiem Platz in deinem Inventar.")
                })
                context.player.playNoSound()
                return@onClick
            }

            context.player.inventory.removeItem(shulker.asQuantity(1))
            handleShulkerWithdrawal(context, context.player, shulker)
        }

        render.layoutSlot('B', continueItem).onClick { context ->
            context.playGeneralClickSound()
            if (!context.player.canEditShopStorageFromCurrentView()) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                context.player.playNoSound()
                return@onClick
            }

            val toRemove = localAmountState.get(context)

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

    private fun handleShulkerWithdrawal(
        context: SlotClickContext,
        player: Player,
        shulker: ItemStack
    ) {
        val shop = shopState.get(context)
        plugin.launch {
            ShopService.blockShop(shop)

            try {
                val updatedShop = ShopService.getShop(shop.shopUuid)

                if (updatedShop == null) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Der Shop existiert nicht mehr.")
                    }
                    withContext(plugin.entityDispatcher(player)) {
                        giveOrDrop(player, shulker)
                    }
                    return@launch
                }

                val toRemove = minOf(
                    updatedShop.storedItemCount,
                    shulker.shulkerFreeCapacityFor(updatedShop.item)
                )

                if (toRemove <= 0) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Der Shop hat keine Items auf Lager.")
                    }
                    withContext(plugin.entityDispatcher(player)) {
                        giveOrDrop(player, shulker)
                    }
                    return@launch
                }

                val filledShulkers = createFilledShulkerPreservingMeta(
                    shulker, updatedShop.item, toRemove
                )

                withContext(plugin.entityDispatcher(player)) {
                    giveOrDrop(player, filledShulkers)

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
                    AuxProtectHook.logWithdraw(player, shop, toRemove)
                }

                player.sendActionBar(buildText {
                    appendSuccessPrefix()
                    success("Du hast ")
                    variableValue("$toRemove Items")
                    success(" per Shulker-Box ausgelagert.")
                })

                withContext(plugin.entityDispatcher(player)) {
                    val refreshedShop = shopState.get(context).updatedShop

                    if (refreshedShop == null) {
                        player.sendText {
                            appendErrorPrefix()
                            error("Der Shop existiert nicht mehr.")
                        }
                        player.closeInventory()
                        return@withContext
                    }

                    context.openForPlayer(
                        ItemStorageView::class.java,
                        ImmutableMap.of(
                            "edit-shop",
                            refreshedShop
                        )
                    )
                }
            } catch (_: Throwable) {
                player.sendText {
                    appendErrorPrefix()
                    error("Ein Fehler ist aufgetreten. Bitte versuche es erneut.")
                }

                withContext(plugin.entityDispatcher(player)) {
                    val refreshedShop = shopState.get(context).updatedShop

                    if (refreshedShop == null) {
                        player.sendText {
                            appendErrorPrefix()
                            error("Der Shop existiert nicht mehr.")
                        }
                        player.closeInventory()
                        return@withContext
                    }

                    context.openForPlayer(
                        ItemStorageView::class.java,
                        ImmutableMap.of(
                            "edit-shop",
                            refreshedShop
                        )
                    )
                }
            } finally {
                ShopService.unblockShop(shop)
            }
        }
    }

    private fun giveOrDrop(player: Player, item: ItemStack) {
        val leftover = player.inventory.addItem(item)
        leftover.values.forEach { rest ->
            player.world.dropItem(player.location, rest).owner = player.uniqueId
        }
    }

    private fun createFilledShulkerPreservingMeta(
        originalShulker: ItemStack, shopItem: ItemStack, amount: Int
    ): ItemStack {
        val filledShulker = originalShulker.asQuantity(1)
        val meta = filledShulker.itemMeta as BlockStateMeta
        val state = meta.blockState as ShulkerBox

        var remaining = amount
        val stacks = mutableListOf<ItemStack>()
        while (remaining > 0) {
            val stackSize = minOf(shopItem.maxStackSize, remaining)
            stacks += shopItem.clone().apply { this.amount = stackSize }
            remaining -= stackSize
        }
        state.inventory.addItem(*stacks.toTypedArray())

        meta.blockState = state
        filledShulker.itemMeta = meta
        return filledShulker
    }

    private val quitItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName { error("Abbrechen".toSmallCaps(), TextDecoration.BOLD) }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private val shulkerSlotItem = buildItem(Material.SHULKER_BOX) {
        displayName {
            shopColored("Shulker-Box füllen", TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                appendBlob()
                shopColored("Klicke eine leere Shulker-Box in")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("deinem Inventar an, um sie mit")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("Items aus dem Lager zu befüllen.")
            }
            emptyLine()
            line {
                appendBlob()
                shopColored("Maximal 27 Stapel")
                spacer(" pro Box.")
            }
            line {
                appendBlob()
                shopColored("Bei weniger Lagerbestand wird")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("die Box teilweise befüllt.")
            }
        }
    }

    private fun handleIncrement(render: RenderContext, context: SlotClickContext, delta: Int) {
        val currentStock = shopState.get(context)
            ?.shopUuid
            ?.let(ShopService::getShop)
            ?.storedItemCount ?: 0

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
            shopColored(
                "${localAmountState.get(context)}/" + shopState.get(context)
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
}