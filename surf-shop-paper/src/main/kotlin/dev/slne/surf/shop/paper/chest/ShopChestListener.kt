package dev.slne.surf.shop.paper.chest

import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.core.service.staticShopChestService
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.StaticShopState
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

object ShopChestListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!ShopChestRecipe.isShopChest(item)) return

        val block = event.blockPlaced
        val player = event.player

        plugin.launch {
            staticShopChestService.createChest(
                shopUuid = null,
                placedBy = player.uniqueId,
                worldName = block.world.name,
                x = block.x,
                y = block.y,
                z = block.z
            )

            player.sendText {
                appendSuccessPrefix()
                success("Shop Chest platziert! Klicke auf die Kiste, um einen Shop zuzuweisen.")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val chest = staticShopChestService.getChestAt(
            block.world.name,
            block.x,
            block.y,
            block.z
        ) ?: return

        val player = event.player

        if (chest.placedBy != player.uniqueId) {
            event.isCancelled = true
            player.sendText {
                appendErrorPrefix()
                error("Du kannst diese Shop Chest nicht abbauen, da du sie nicht platziert hast.")
            }
            player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_NO)
            }
            return
        }

        plugin.launch {
            staticShopChestService.deleteChest(chest.chestUuid)

            player.sendText {
                appendSuccessPrefix()
                success("Shop Chest entfernt.")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return

        val block: Block = event.clickedBlock ?: return
        val chest = staticShopChestService.getChestAt(
            block.world.name,
            block.x,
            block.y,
            block.z
        ) ?: return

        event.isCancelled = true
        val player = event.player

        val shopUuid = chest.shopUuid
        if (shopUuid == null) {
            if (chest.placedBy == player.uniqueId) {
                ChestShopEditState.setChest(player.uniqueId, chest)
                viewFrame.open(
                    ShopChestSetupView::class.java,
                    player,
                    ImmutableMap.of("shop-chest", chest)
                )
            } else {
                player.sendText {
                    appendErrorPrefix()
                    error("Diese Shop Chest wurde noch nicht konfiguriert.")
                }
                player.playSound(true) {
                    type(Sound.ENTITY_VILLAGER_NO)
                }
            }
            return
        }

        val shop = shopService.loadedShops.firstOrNull { it.shopUuid == shopUuid }
        if (shop == null) {
            player.sendText {
                appendErrorPrefix()
                error("Der zugewiesene Shop existiert nicht mehr.")
            }
            player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_NO)
            }
            return
        }

        if (shop.seller == player.uniqueId) {
            StaticShopState.setInStaticShop(player.uniqueId, false)
            ChestShopEditState.setChest(player.uniqueId, chest)
            viewFrame.open(
                ShopChestSetupView::class.java,
                player,
                ImmutableMap.of("shop-chest", chest)
            )
        } else {
            StaticShopState.setInStaticShop(player.uniqueId, true)
            ChestShopEditState.setChest(player.uniqueId, null)
            viewFrame.open(
                BuyShopItemView::class.java,
                player,
                ImmutableMap.of("buy-shop", shop)
            )
        }
    }
}
