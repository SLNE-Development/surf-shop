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
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.IFContext
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import kotlin.math.max
import kotlin.math.min

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
                "UOOOBOOOO"
            )
            .cancelOnClick().cancelOnDrop().cancelOnDrag()
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
            val cursorItem = context.clickOrigin.currentItem ?: return@onClick
            if (!cursorItem.type.name.endsWith("SHULKER_BOX")) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Lege eine leere Shulker-Box auf deinen Cursor.")
                })
                context.player.playNoSound()
                return@onClick
            }

            val meta = cursorItem.itemMeta
            if (meta !is BlockStateMeta || meta.blockState !is ShulkerBox) {
                return@onClick
            }

            val boxState = meta.blockState as ShulkerBox
            val contents = boxState.inventory.contents ?: return@onClick
            if (contents.any { it != null && !it.type.isAir }) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Die Shulker-Box muss leer sein.")
                })
                context.player.playNoSound()
                return@onClick
            }

            context.clickOrigin.currentItem = ItemStack.empty()
            handleShulkerWithdrawal(context, context.player, cursorItem.type)
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
                    ShopService.loadedShops.find { it.shopUuid == shop.shopUuid }

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
                                        player.world.dropItem(player.location, rest)
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
                                        player.world.dropItem(player.location, rest)
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

    override fun onClick(click: SlotClickContext) {
        if (click.clickedContainer.isEntityContainer) {
            if (!click.player.canEditShopStorageFromCurrentView()) {
                click.player.sendText {
                    appendErrorPrefix()
                    error("Das Lager kannst du nur am Spawn bearbeiten.")
                }
                click.player.playNoSound()
                return
            }

            val item = click.item ?: return

            if (item.type.name.endsWith("SHULKER_BOX")) {
                val meta = item.itemMeta
                if (meta is BlockStateMeta) {
                    val state = meta.blockState
                    if (state is ShulkerBox) {
                        val contents = state.inventory.contents ?: return
                        if (contents.any { it != null && !it.type.isAir }) {
                            click.player.sendActionBar(buildText {
                                appendErrorPrefix()
                                error("Die Shulker-Box muss leer sein.")
                            })
                            click.player.playNoSound()
                            return
                        }

                        click.clickOrigin.currentItem = ItemStack.empty()
                        handleShulkerWithdrawal(click, click.player, item.type)
                    }
                }
            }
        }
    }

    private fun handleShulkerWithdrawal(
        context: IFContext,
        player: Player,
        shulkerType: Material
    ) {
        if (!player.canEditShopStorageFromCurrentView()) {
            player.sendText {
                appendErrorPrefix()
                error("Das Lager kannst du nur am Spawn bearbeiten.")
            }
            player.playNoSound()
            return
        }

        val shop = shopState.get(context)
        ShopService.blockShop(shop)

        plugin.launch {
            val updatedShop =
                ShopService.loadedShops.find { it.shopUuid == shop.shopUuid }

            if (updatedShop == null) {
                player.sendText {
                    appendErrorPrefix()
                    error("Der Shop existiert nicht mehr.")
                }
                ShopService.unblockShop(shop)
                return@launch
            }

            val toRemove = minOf(updatedShop.storedItemCount, 27 * updatedShop.item.maxStackSize)

            if (toRemove <= 0) {
                player.sendText {
                    appendErrorPrefix()
                    error("Der Shop hat keine Items auf Lager.")
                }
                ShopService.unblockShop(updatedShop)

                withContext(plugin.entityDispatcher(player)) {
                    val emptyShulker = ItemStack(shulkerType)
                    val leftover = player.inventory.addItem(emptyShulker)
                    if (leftover.isNotEmpty()) {
                        leftover.values.forEach { rest ->
                            val dropped = player.world.dropItem(
                                player.location, rest
                            )
                            dropped.owner = player.uniqueId
                        }
                    }
                }
                return@launch
            }

            val filledShulker = createFilledShulker(
                shulkerType, updatedShop.item, toRemove
            )

            withContext(plugin.entityDispatcher(player)) {
                val leftover = player.inventory.addItem(filledShulker)
                if (leftover.isNotEmpty()) {
                    leftover.values.forEach { rest ->
                        val dropped = player.world.dropItem(
                            player.location, rest
                        )
                        dropped.owner = player.uniqueId
                    }
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
            ShopService.unblockShop(updatedShop)

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
        }
    }

    private fun createFilledShulker(
        shulkerType: Material, shopItem: ItemStack, amount: Int
    ): ItemStack {
        val filledShulker = ItemStack(shulkerType)
        val meta = filledShulker.itemMeta as BlockStateMeta
        val state = meta.blockState as ShulkerBox
        val inv = state.inventory

        var remaining = amount
        val maxStack = shopItem.maxStackSize
        var slotIndex = 0

        while (remaining > 0 && slotIndex < 27) {
            val stack = shopItem.clone()
            val stackSize = minOf(maxStack, remaining)
            stack.amount = stackSize
            inv.setItem(slotIndex, stack)
            remaining -= stackSize
            slotIndex++
        }

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
                shopColored("Klicke mit einer leeren Shulker-Box")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("auf diesen Slot, um sie mit Items")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("aus dem Lager zu befüllen.")
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
            emptyLine()
            line {
                appendBlob()
                shopColored("Alternativ kannst du eine leere")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("Shulker-Box in deinem Inventar")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("anklicken.")
            }
        }
    }

    private fun handleIncrement(render: RenderContext, context: SlotClickContext, delta: Int) {
        val currentStock = ShopService.loadedShops.find {
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
            shopColored(("${localAmountState.get(context)}/" + ShopService.loadedShops.find {
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