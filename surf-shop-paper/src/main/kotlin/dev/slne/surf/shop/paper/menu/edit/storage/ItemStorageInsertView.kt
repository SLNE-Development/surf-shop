package dev.slne.surf.shop.paper.menu.edit.storage

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockColumn
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.menu.canEditShopStorageFromCurrentView
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

val itemStorageInsertView: AbstractSurfView = surfView("Items einlagern") {
    val shopState = initialState<Shop>("edit-shop")
    val itemInsertedState = mutableState(0)

    settings {
        rows(5)
        cancelOnClick()
        cancelOnDrag()
        cancelOnDrop()
        cancelOnPickup(false)
    }

    containerDefaults {
        blockRow(1)
        blockColumn(0)
        blockColumn(8)
        blockRow(5)
    }

    onInit {
        layout(
            "         ",
            " SSSSSSS ",
            " SSSSSSS ",
            " SSSSSSS ",
            "         ",
        )
    }

    onFirstRender {
        layoutSlot('S', ItemStack.empty()).onClick { context ->
            context.isCancelled = false
        }

        slot(1, 5).renderWith {
            explainItem(shopState[this].storedItemCount)
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
            val amount = item.amount

            val currentShop = shopState[this@onClick]
            val newShop = currentShop.copy(
                storedItemCount = currentShop.storedItemCount + amount
            )

            shopState[this@onClick] = newShop
            ShopService.saveShop(newShop)
            itemInsertedState[this@onClick] = itemInsertedState[this@onClick] + amount

            if (plugin.auxProtectHook) {
                AuxProtectHook.logDeposit(this@onClick.player, currentShop, amount)
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
