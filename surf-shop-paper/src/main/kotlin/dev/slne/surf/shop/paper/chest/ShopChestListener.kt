package dev.slne.surf.shop.paper.chest

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.core.service.staticShopChestService
import dev.slne.surf.shop.paper.hook.FancyHologramsHook
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.StaticShopState
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.event.cancel
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.event.world.StructureGrowEvent

object ShopChestListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!ShopChestRecipe.isShopChest(item)) return

        val block = event.blockPlaced
        val player = event.player

        player.playSound(true) {
            type(Sound.BLOCK_LEVER_CLICK)
        }

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
    fun onShopBreak(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return


        if (!event.action.isLeftClick) {
            return
        }

        val chest = staticShopChestService.getChestAt(
            block.world.name,
            block.x,
            block.y,
            block.z
        ) ?: return

        val player = event.player

        event.isCancelled = true

        if (chest.placedBy != player.uniqueId && !player.hasPermission(PermissionRegistry.CHEST_SHOP_BREAK_BYPASS)) {
            player.sendText {
                appendErrorPrefix()
                error("Du kannst diese Shop Chest nicht abbauen, da du sie nicht platziert hast.")
            }
            player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_NO)
            }
            return
        }

        if (!player.isSneaking) {
            player.sendText {
                appendErrorPrefix()
                error("Du musst schleichen (Shift + Klick), um eine Shop Chest abzubauen.")
            }
            player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_NO)
            }
            return
        }

        player.playSound(true) {
            type(Sound.ENTITY_ITEM_PICKUP)
        }

        plugin.launch {
            withContext(plugin.regionDispatcher(block.location)) {
                block.type = Material.AIR
            }

            val hasToDrop = withContext(plugin.entityDispatcher(player)) {
                player.inventory.addItem(ShopChestRecipe.shopChestItem).isNotEmpty()
            }

            if (hasToDrop) {
                withContext(plugin.regionDispatcher(block.location)) {
                    block.world.dropItem(
                        block.location,
                        ShopChestRecipe.shopChestItem
                    ).owner = player.uniqueId
                }
            }

            staticShopChestService.deleteChest(chest.chestUuid)

            if (plugin.hasFancyHolograms) {
                FancyHologramsHook.deleteHologramIfExists(chest.chestUuid)
            }

            player.sendText {
                appendSuccessPrefix()
                success("Die Shop Kiste wurde entfernt.")
            }
        }
    }

    @EventHandler
    fun onShopDestroy(event: BlockBreakEvent) {
        val block = event.block
        val chest = staticShopChestService.getChestAt(
            block.world.name,
            block.x,
            block.y,
            block.z
        ) ?: return

        val player = event.player

        event.isCancelled = true

        if (chest.placedBy != player.uniqueId && !player.hasPermission(PermissionRegistry.CHEST_SHOP_BREAK_BYPASS)) {
            player.sendText {
                appendErrorPrefix()
                error("Du kannst diese Shop Chest nicht abbauen.")
            }
            return
        }

        if (!player.isSneaking) {
            player.sendText {
                appendErrorPrefix()
                error("Du musst schleichen (Shift + Klick), um eine Shop Chest abzubauen.")
            }
            player.playSound(true) {
                type(Sound.ENTITY_VILLAGER_NO)
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

    @EventHandler
    fun onBlockDestroy(event: BlockDestroyEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { isStaticChest(it) }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { isStaticChest(it) }
    }

    @EventHandler
    fun onBlockFade(event: BlockFadeEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockBurn(event: BlockBurnEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockForm(event: BlockFormEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onEntityBlockForm(event: EntityBlockFormEvent) {
        if (isStaticChest(event.block)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockPistonExtend(event: BlockPistonExtendEvent) {
        if (event.blocks.any { isStaticChest(it) }) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockPistonRetract(event: BlockPistonRetractEvent) {
        if (event.blocks.any { isStaticChest(it) }) {
            event.cancel()
        }
    }

    @EventHandler
    fun onStructureGrow(event: StructureGrowEvent) {
        event.blocks.removeIf { isStaticChest(it.block) }
    }

    @EventHandler
    fun onPortalCreate(event: PortalCreateEvent) {
        if (event.blocks.any { isStaticChest(it.block) }) {
            event.cancel()
        }

    }


    fun isStaticChest(block: Block) = staticShopChestService.getChestAt(
        block.world.name,
        block.x,
        block.y,
        block.z
    ) != null
}
