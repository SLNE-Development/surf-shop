package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.CloseContext
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import me.devnatan.inventoryframework.state.MutableState
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object ItemStorageInsertView : View() {
    private val shopState = initialState<Shop>("edit-shop")
    private val localShopState: MutableState<Shop> = mutableState(Shop.empty())
    private val itemInsertedState = mutableState(0)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Items einlagern".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOQOOOO", "OSSSSSSSO", "OSSSSSSSO", "OSSSSSSSO", "OOOOBOOOO")
            .cancelOnClick().cancelOnDrop().cancelOnDrag()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        localShopState.set(shopState.get(render), render)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()

            context.openForPlayer(
                ItemStorageView::class.java,
                ImmutableMap.of("edit-shop", localShopState.get(render))
            )
        }

        render.layoutSlot('S', ItemStack.empty()).onClick { context ->
            context.isCancelled = false
        }

        render.layoutSlot('Q').renderWith {
            explainItem(localShopState.get(render).storedItemCount)
        }.watch(localShopState)
    }

    override fun onClick(click: SlotClickContext) {
        if (!click.clickedContainer.isEntityContainer) {
            return
        }

        if (!click.player.canEditShopStorageFromCurrentView()) {
            click.player.sendText {
                appendErrorPrefix()
                error("Das Lager kannst du nur am Spawn bearbeiten.")
            }
            click.player.playNoSound()
            return
        }

        val item = click.item ?: return
        val shop = localShopState.get(click)

        if (!item.isSimilar(shop.item)) {
            if (!Tag.SHULKER_BOXES.isTagged(item.type)) {
                return
            }

            if (!click.player.canUseShulkerFeature()) {
                click.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Dir fehlt die Berechtigung, um Shulker-Boxen im Shop zu nutzen.")
                })
                click.player.playNoSound()
                return
            }

            val returnedShulker = item.asQuantity(1)
            val returnMeta = returnedShulker.itemMeta as? BlockStateMeta ?: return
            val returnState = returnMeta.blockState as? ShulkerBox ?: return
            val boxInventory = returnState.inventory

            var totalItems = 0
            for (i in 0 until boxInventory.size) {
                val slotItem = boxInventory.getItem(i) ?: continue
                if (slotItem.type.isAir || !slotItem.isSimilar(shop.item)) {
                    continue
                }
                totalItems += slotItem.amount
                boxInventory.setItem(i, null)
            }

            if (totalItems <= 0) {
                click.player.sendActionBar(buildText {
                    appendErrorPrefix()
                    error("Die Shulker-Box enthält keine passenden Items.")
                })
                click.player.playNoSound()
                return
            }

            returnMeta.blockState = returnState
            returnedShulker.itemMeta = returnMeta

            click.clickOrigin.currentItem = returnedShulker
            click.player.updateInventory()

            plugin.launch {
                val currentShop = localShopState.get(click)
                val newShop = currentShop.copy(
                    storedItemCount = currentShop.storedItemCount + totalItems
                )
                localShopState.set(newShop, click)
                ShopService.saveShop(newShop)
                itemInsertedState.set(
                    itemInsertedState.get(click) + totalItems, click
                )

                if (plugin.auxProtectHook) {
                    AuxProtectHook.logDeposit(click.player, currentShop, totalItems)
                }

                click.player.sendActionBar(buildText {
                    appendSuccessPrefix()
                    success("Du hast ")
                    variableValue("$totalItems Items")
                    success(" per Shulker-Box eingelagert.")
                })

                click.player.playSound(true) {
                    type(Sound.ENTITY_PLAYER_LEVELUP)
                }
            }
            return
        }

        click.isCancelled = false
        click.clickOrigin.currentItem = ItemStack.empty()

        plugin.launch {
            val amount = item.amount

            val currentShop = localShopState.get(click)
            val newShop = currentShop.copy(
                storedItemCount = currentShop.storedItemCount + amount
            )

            localShopState.set(newShop, click)

            ShopService.saveShop(newShop)

            itemInsertedState.set(
                itemInsertedState.get(click) + amount,
                click
            )

            if (plugin.auxProtectHook) {
                AuxProtectHook.logDeposit(click.player, currentShop, amount)
            }

            click.player.sendActionBar(buildText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("$amount Items")
                success(" eingelagert.")
            })

            click.player.playSound(true) {
                type(Sound.ENTITY_PLAYER_LEVELUP)
            }
        }
    }

    override fun onClose(close: CloseContext) {
        val added = itemInsertedState.get(close)
        if (added > 0) {
            close.player.sendText {
                appendSuccessPrefix()
                success("Du hast ")
                variableValue("${added}x ")
                translatable(shopState.get(close).item.type.translationKey())
                success(" eingelagert.")
            }

            close.player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_YES)
            }
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Zurück")
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
}
