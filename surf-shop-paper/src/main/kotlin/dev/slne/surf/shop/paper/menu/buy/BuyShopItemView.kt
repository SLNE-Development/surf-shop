package dev.slne.surf.shop.paper.menu.buy

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.core.paper.util.updatedShop
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.displayKey
import dev.slne.surf.shop.paper.util.formatPriceNice
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object BuyShopItemView : View() {
    private val shopState = initialState<Shop>("buy-shop")
    private val amountState = mutableState(1)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Items kaufen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout(
                "OOOOOOOOO",
                "O U I C O",
                "OOOOBOOOO"
            )
            .cancelOnClick().cancelOnDrop().cancelOnDrag()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('U').renderWith { shulkerSlotItem }.onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canUseShopTransactionsFromCurrentView()) {
                context.player.sendText {
                    appendInfoPrefix()
                    info("Dieser Shop ist hier nur zur Ansicht. Zum Kaufen musst du zum Spawn.")
                }
                context.player.playNoSound()
                return@onClick
            }

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
            val contents = boxState.inventory.contents
            if (contents.any { it != null && !it.type.isAir }) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Die Shulker-Box muss leer sein.")
                })
                context.player.playNoSound()
                return@onClick
            }

            val currentShop = shopState.get(context).updatedShop ?: run {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Dieser Shop existiert nicht mehr!")
                }
                context.player.playNoSound()
                plugin.launch { openListView(render) }
                return@onClick
            }

            if (currentShop.isBlocked) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                }
                context.player.playNoSound()
                return@onClick
            }

            if (currentShop.storedItemCount <= 0) {
                context.player.sendText {
                    appendErrorPrefix()
                    error("Dieser Shop ist ausverkauft!")
                }
                context.player.playNoSound()
                return@onClick
            }

            val maxPerSlot = currentShop.item.maxStackSize
            val shulkerMax = 27 * maxPerSlot
            val buyAmount = minOf(currentShop.storedItemCount, shulkerMax)

            context.clickOrigin.currentItem = ItemStack.empty()
            context.player.closeInventory()

            plugin.launch {
                when (val result = DealService.buy(
                    context.player.uniqueId, currentShop, buyAmount
                )) {
                    is Deal.DealResult.Success -> {
                        val boughtAmount = result.deal.amount

                        withContext(plugin.entityDispatcher(context.player)) {
                            val toRemove = currentShop.item.clone().apply {
                                amount = boughtAmount
                            }
                            val notRemoved =
                                context.player.inventory.removeItem(toRemove)
                            val removedAmount =
                                boughtAmount - notRemoved.values.sumOf { it.amount }

                            if (removedAmount > 0) {
                                val filledShulker = createFilledShulker(
                                    cursorItem.type, currentShop.item, removedAmount
                                )
                                val leftover =
                                    context.player.inventory.addItem(filledShulker)
                                if (leftover.isNotEmpty()) {
                                    withContext(
                                        plugin.regionDispatcher(context.player.location)
                                    ) {
                                        leftover.values.forEach {
                                            context.player.world.dropItem(
                                                context.player.location, it
                                            ).owner = context.player.uniqueId
                                        }
                                    }
                                }
                            }

                            context.player.playSound(true) {
                                    type(Sound.ENTITY_CHICKEN_EGG)
                                }
                        }

                        context.player.sendText {
                            appendSuccessPrefix()
                            success(
                                "Du hast erfolgreich ${result.deal.amount} Items für ${
                                    formatPriceNice(
                                        result.deal.amount * currentShop.pricePerItem
                                    )
                                } gekauft und in einer Shulker-Box erhalten!"
                            )
                        }

                        Bukkit.getPlayer(currentShop.seller)?.sendText {
                            appendInfoPrefix()
                            variableValue(context.player.name)
                            info(" hat gerade ")
                            append {
                                if (result.deal.amount > 1) {
                                    variableValue("${result.deal.amount}x ")
                                }
                                append(currentShop.item.displayName())
                                hoverEvent(currentShop.item.asHoverEvent())
                            }
                            info(" gekauft.")
                            spacer(
                                " (${formatPriceNice(currentShop.pricePerItem * result.deal.amount)} - 3% Steuern)"
                            )
                        }

                        openListView(render)
                    }

                    Deal.DealResult.InsufficientStock -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }

                    Deal.DealResult.OtherInsufficientFounds,
                    Deal.DealResult.SelfInsufficientFounds -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }

                    Deal.DealResult.ShopBlocked -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }

                    Deal.DealResult.ShopDeleted -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Dieser Shop existiert nicht mehr!")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }

                    Deal.DealResult.TransactionFailed -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }

                    Deal.DealResult.PlayerNotFound -> {
                        returnEmptyShulker(context, cursorItem)
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
                        }
                        context.player.playNoSound()
                        openListView(render)
                    }
                }
            }
        }

        render.layoutSlot('I')
            .renderWith {
                val initialShop = shopState.get(render)
                val shop = initialShop.updatedShop ?: initialShop
                shop.item.clone().apply {
                    val oldLore = lore()?.toMutableList() ?: mutableListOf()
                    val newEntries = mutableListOf<Component>()

                    val amount = amountState.get(render)

                    newEntries.add(Component.empty())
                    newEntries.add(buildText {
                        shopColored("Shopinformationen".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Einzelpreis: ")
                        variableValue(shop.pricePerItem)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Auf Lager: ")
                        if (shop.storedItemCount > 0) {
                            variableValue("${shop.storedItemCount} Items")
                        } else {
                            error("Ausverkauft")
                        }
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Verkäufer: ")
                        variableValue(shop.sellerName)
                    })

                    newEntries.add(Component.empty())

                    newEntries.add(buildText {
                        shopColored("Kaufsinformationen".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Anzahl: ")
                        variableValue(amount)
                    })

                    newEntries.add(buildText {
                        spacer("-")
                        appendSpace()
                        shopColored("Gesamtpreis: ")
                        variableValue(formatPriceNice(amount * shop.pricePerItem))
                    })

                    newEntries.add(Component.empty())
                    newEntries.add(buildText {
                        shopColored("Anzahl".toSmallCaps(), TextDecoration.BOLD)
                    })

                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.left")
                        darkSpacer(":")
                        variableValue(" +1".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.left")
                        spacer(" + ")
                        white("SHIFT".toSmallCaps())
                        darkSpacer(":")
                        variableValue(" +64".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.right")
                        darkSpacer(":")
                        variableValue(" -1".toSmallCaps())
                    })
                    newEntries.add(buildText {
                        appendBlob()
                        displayKey("key.mouse.right")
                        spacer(" + ")
                        white("SHIFT".toSmallCaps())
                        darkSpacer(":")
                        variableValue(" -64".toSmallCaps())
                    })

                    lore(oldLore + newEntries)
                }
            }
            .updateOnStateChange(amountState)
            .onClick { context ->
                context.playGeneralClickSound()

                if (!context.player.canUseShopTransactionsFromCurrentView()) {
                    context.player.sendText {
                        appendInfoPrefix()
                        info("Dieser Shop ist hier nur zur Ansicht. Zum Kaufen musst du zum Spawn.")
                    }
                    context.player.playNoSound()
                    return@onClick
                }

                val currentShop = shopState.get(context).updatedShop ?: run {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop existiert nicht mehr!")
                    }

                    context.player.playNoSound()
                    if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                        StaticShopState.setInStaticShop(context.player.uniqueId, false)
                        context.player.closeInventory()
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(OwnShopsListView::class.java)
                    } else {
                        context.openForPlayer(ShopListView::class.java)
                    }
                    return@onClick
                }

                var amount = amountState.get(context)

                if (currentShop.storedItemCount <= 0) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop ist ausverkauft!")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                if (amount > currentShop.storedItemCount) {
                    amount = currentShop.storedItemCount
                    amountState.set(amount, context)
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Die Anzahl wurde auf den verfügbaren Lagerbestand (${currentShop.storedItemCount}) angepasst!")
                    }

                    context.player.playNoSound()

                    if (!context.isRightClick && !context.isShiftRightClick) {
                        return@onClick
                    }
                }

                if (context.isShiftLeftClick) {
                    if (currentShop.storedItemCount < amount + 64) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount + 64, context)
                    return@onClick
                }

                if (context.isLeftClick) {
                    if (currentShop.storedItemCount < amount + 1) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount + 1, context)
                    return@onClick
                }

                if (context.isShiftRightClick) {
                    if (amount - 64 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }

                    amountState.set(amount - 64, context)
                    return@onClick
                }

                if (context.isRightClick) {
                    if (amount - 1 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amount - 1, context)
                    return@onClick
                }
            }

        render.layoutSlot('C')
            .renderWith {
                MenuHeads.CHECK.clone().apply {
                    displayName {
                        shopColored("Kaufen")
                    }

                    buildLore {
                        emptyLine()
                        line {
                            appendBlob()
                            spacer(
                                "Klicke um die Items zu kaufen.".toSmallCaps(),
                                TextDecoration.BOLD
                            )
                        }

                        emptyLine()
                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Anzahl: ")
                            variableValue(amountState.get(render))
                        }

                        line {
                            spacer("-")
                            appendSpace()
                            shopColored("Gesamtpreis: ")
                            variableValue(
                                amountState.get(render) * shopState.get(render).pricePerItem
                            )
                        }
                    }
                }
            }
            .updateOnStateChange(amountState)
            .onClick { context ->
                context.playGeneralClickSound()

                if (!context.player.canUseShopTransactionsFromCurrentView()) {
                    context.player.sendText {
                        appendInfoPrefix()
                        info("Dieser Shop ist hier nur zur Ansicht. Zum Kaufen musst du zum Spawn.")
                    }
                    context.player.playNoSound()
                    return@onClick
                }

                val shop = shopState.get(context).updatedShop ?: run {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Dieser Shop existiert nicht mehr!")
                    }

                    context.player.playNoSound()
                    if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                        StaticShopState.setInStaticShop(context.player.uniqueId, false)
                        context.player.closeInventory()
                    } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                        context.openForPlayer(OwnShopsListView::class.java)
                    } else {
                        context.openForPlayer(ShopListView::class.java)
                    }
                    return@onClick
                }

                val amount = amountState.get(context)

                if (shop.isBlocked) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                if (shop.storedItemCount < amount) {
                    context.player.sendText {
                        appendErrorPrefix()
                        error("Es sind nicht genügend Items auf Lager!")
                    }

                    context.player.playNoSound()
                    return@onClick
                }

                context.player.closeInventory()

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logBuy(context.player, shop, amount)
                }

                plugin.launch {
                    when (val result = DealService.buy(
                        context.player.uniqueId, shop, amount
                    )) {
                        Deal.DealResult.InsufficientStock -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es sind nicht genügend Items auf Lager!")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        Deal.DealResult.OtherInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        Deal.DealResult.SelfInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        Deal.DealResult.ShopBlocked -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        Deal.DealResult.ShopDeleted -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Dieser Shop existiert nicht mehr!")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        is Deal.DealResult.Success -> {
                            context.player.sendText {
                                appendSuccessPrefix()
                                success(
                                    "Du hast erfolgreich ${result.deal.amount} Items für ${
                                        formatPriceNice(
                                            result.deal.amount * shop.pricePerItem
                                        )
                                    } gekauft!"
                                )
                            }

                            Bukkit.getPlayer(shop.seller)?.sendText {
                                appendInfoPrefix()
                                variableValue(context.player.name)
                                info(" hat gerade ")
                                append {
                                    if (result.deal.amount > 1) {
                                        variableValue("${result.deal.amount}x ")
                                    }
                                    append(shop.item.displayName())
                                    hoverEvent(shop.item.asHoverEvent())
                                }
                                info(" gekauft.")
                                spacer(
                                    " (${formatPriceNice(shop.pricePerItem * result.deal.amount)} - 3% Steuern)"
                                )
                            }

                            openListView(render)
                        }

                        Deal.DealResult.TransactionFailed -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }

                        Deal.DealResult.PlayerNotFound -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
                            }

                            context.player.playNoSound()
                            openListView(render)
                        }
                    }
                }
            }
        render.layoutSlot('B', MenuHeads.CROSS.clone().apply {
            displayName {
                error("Abbrechen")
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                StaticShopState.setInStaticShop(context.player.uniqueId, false)
                context.player.closeInventory()
            } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                context.openForPlayer(OwnShopsListView::class.java)
            } else {
                context.openForPlayer(ShopListView::class.java)
            }
        }
    }

    private val shulkerSlotItem = ItemStack(Material.SHULKER_BOX).apply {
        displayName {
            shopColored("Shulker-Box Kauf", TextDecoration.BOLD)
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
                shopColored("auf diesen Slot, um die Items")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("beim Kauf direkt in die Box zu")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("packen.")
            }
            emptyLine()
            line {
                appendBlob()
                shopColored("Maximal 27 Stapel")
                spacer(" pro Box.")
            }
            line {
                appendBlob()
                shopColored("Bei geringerem Lagerbestand")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("wird die Box teilweise befullt.")
            }
            emptyLine()
            line {
                appendBlob()
                shopColored("Der Kaufpreis wird wie gewohnt")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("von deinem Konto abgezogen.")
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

    private suspend fun returnEmptyShulker(
        context: me.devnatan.inventoryframework.context.SlotClickContext,
        shulkerItem: ItemStack
    ) = withContext(plugin.entityDispatcher(context.player)) {
        val emptyShulker = ItemStack(shulkerItem.type)
        val leftover = context.player.inventory.addItem(emptyShulker)
        if (leftover.isNotEmpty()) {
            withContext(plugin.regionDispatcher(context.player.location)) {
                leftover.values.forEach {
                    context.player.world.dropItem(
                        context.player.location, it
                    ).owner = context.player.uniqueId
                }
            }
        }
    }

    private suspend fun openListView(context: RenderContext) =
        withContext(plugin.entityDispatcher(context.player)) {
            if (StaticShopState.isInStaticShop(context.player.uniqueId)) {
                StaticShopState.setInStaticShop(context.player.uniqueId, false)
                context.player.closeInventory()
            } else if (OwnShopState.isInOwn(context.player.uniqueId)) {
                viewFrame.open(OwnShopsListView::class.java, context.player)
            } else {
                viewFrame.open(ShopListView::class.java, context.player)
            }
        }
}