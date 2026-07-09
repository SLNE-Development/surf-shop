package dev.slne.surf.shop.paper.menu.buy

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
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
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
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
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('U').renderWith { shulkerSlotItem }.onClick { context ->
            context.playGeneralClickSound()

            if (!context.player.canUseShulkerFeature()) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Dir fehlt die Berechtigung, um Shulker-Boxen im Shop zu nutzen.")
                })
                context.player.playNoSound()
                return@onClick
            }

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
                plugin.launch { openListView(context.player) }
                return@onClick
            }

            if (currentShop.isBlocked) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                })
                context.player.playNoSound()
                return@onClick
            }

            if (currentShop.storedItemCount <= 0) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Dieser Shop ist ausverkauft!")
                })
                context.player.playNoSound()
                return@onClick
            }

            if (Tag.SHULKER_BOXES.isTagged(currentShop.item.type)) {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Du kannst keine Shulker-Boxen in einer Shulker-Box kaufen.")
                })
                context.player.playNoSound()
                return@onClick
            }

            val shulker = context.player.findFillableShulker(currentShop.item)?.asQuantity(1) ?: run {
                context.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Du brauchst eine Shulker-Box mit freiem Platz in deinem Inventar.")
                })
                context.player.playNoSound()
                return@onClick
            }

            val buyAmount = minOf(
                currentShop.storedItemCount,
                shulker.shulkerFreeCapacityFor(currentShop.item)
            )

            context.player.inventory.removeItem(shulker.asQuantity(1))
            context.player.closeInventory()

            plugin.launch {
                completeShulkerPurchase(context.player, currentShop, buyAmount, shulker)
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
                            openListView(context.player)
                        }

                        Deal.DealResult.OtherInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
                        }

                        Deal.DealResult.SelfInsufficientFounds -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
                        }

                        Deal.DealResult.ShopBlocked -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
                        }

                        Deal.DealResult.ShopDeleted -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Dieser Shop existiert nicht mehr!")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
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

                            openListView(context.player)
                        }

                        Deal.DealResult.TransactionFailed -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
                        }

                        Deal.DealResult.PlayerNotFound -> {
                            context.player.sendText {
                                appendErrorPrefix()
                                error("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
                            }

                            context.player.playNoSound()
                            openListView(context.player)
                        }
                        
                        is Deal.DealResult.SuccessWithItems<*> -> {
                            context.player.sendText {
                                appendSuccessPrefix()
                                success("Du hast erfolgreich ${result.deal.amount} Items gekauft!")
                            }
                            openListView(context.player)
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

    private suspend fun completeShulkerPurchase(
        player: Player,
        shop: Shop,
        buyAmount: Int,
        shulker: ItemStack
    ) {
        when (val result = DealService.buyWithoutDelivery(player.uniqueId, shop, buyAmount)) {
            is Deal.DealResult.SuccessWithItems<*> -> {
                @Suppress("UNCHECKED_CAST")
                val purchasedItems = result.items as List<ItemStack>

                withContext(plugin.entityDispatcher(player)) {
                    giveOrDrop(player, createFilledShulkerFromItems(shulker, purchasedItems))

                    player.playSound(true) {
                        type(Sound.ENTITY_CHICKEN_EGG)
                    }
                }

                player.sendText {
                    appendSuccessPrefix()
                    success(
                        "Du hast erfolgreich ${result.deal.amount} Items für ${
                            formatPriceNice(result.deal.amount * shop.pricePerItem)
                        } gekauft und in einer Shulker-Box erhalten!"
                    )
                }

                Bukkit.getPlayer(shop.seller)?.sendText {
                    appendInfoPrefix()
                    variableValue(player.name)
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

                openListView(player)
            }

            is Deal.DealResult.Success -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendSuccessPrefix()
                    success(
                        "Du hast erfolgreich ${result.deal.amount} Items für ${
                            formatPriceNice(result.deal.amount * shop.pricePerItem)
                        } gekauft!"
                    )
                }
                openListView(player)
            }

            Deal.DealResult.InsufficientStock -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Es sind nicht genügend Items auf Lager!")
                }
                player.playNoSound()
                openListView(player)
            }

            Deal.DealResult.OtherInsufficientFounds,
            Deal.DealResult.SelfInsufficientFounds -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast nicht genügend Geld, um diesen Kauf zu tätigen!")
                }
                player.playNoSound()
                openListView(player)
            }

            Deal.DealResult.ShopBlocked -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst derzeit keine Items in diesem Shop kaufen!")
                }
                player.playNoSound()
                openListView(player)
            }

            Deal.DealResult.ShopDeleted -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Dieser Shop existiert nicht mehr!")
                }
                player.playNoSound()
                openListView(player)
            }

            Deal.DealResult.TransactionFailed -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Es ist ein Fehler aufgetreten. (TRANSACTION_FAILED)")
                }
                player.playNoSound()
                openListView(player)
            }

            Deal.DealResult.PlayerNotFound -> {
                returnShulker(player, shulker)
                player.sendText {
                    appendErrorPrefix()
                    error("Es ist ein Fehler aufgetreten. (PLAYER_NOT_FOUND)")
                }
                player.playNoSound()
                openListView(player)
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
                shopColored("Klicke eine leere Shulker-Box in")
            }
            line {
                appendSpace()
                appendBlob()
                shopColored("deinem Inventar an, um die Items")
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
                shopColored("wird die Box teilweise befüllt.")
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

    private fun createFilledShulkerFromItems(
        template: ItemStack, items: List<ItemStack>
    ): ItemStack {
        val filledShulker = template.asQuantity(1)
        val meta = filledShulker.itemMeta as BlockStateMeta
        val state = meta.blockState as ShulkerBox

        state.inventory.addItem(*items.toTypedArray())

        meta.blockState = state
        filledShulker.itemMeta = meta
        return filledShulker
    }

    private fun giveOrDrop(player: Player, item: ItemStack) {
        val leftover = player.inventory.addItem(item)
        leftover.values.forEach { rest ->
            player.world.dropItem(player.location, rest).owner = player.uniqueId
        }
    }

    private suspend fun returnShulker(player: Player, shulker: ItemStack) =
        withContext(plugin.entityDispatcher(player)) {
            giveOrDrop(player, shulker)
        }

    private suspend fun openListView(player: Player) =
        withContext(plugin.entityDispatcher(player)) {
            if (StaticShopState.isInStaticShop(player.uniqueId)) {
                StaticShopState.setInStaticShop(player.uniqueId, false)
                player.closeInventory()
            } else if (OwnShopState.isInOwn(player.uniqueId)) {
                viewFrame.open(OwnShopsListView::class.java, player)
            } else {
                viewFrame.open(ShopListView::class.java, player)
            }
        }
}