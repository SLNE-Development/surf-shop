package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.events.state.ShopCreateEvent;
import dev.slne.surf.shop.api.instance.ShopInstance;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.util.ShopUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ShopPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        final ItemStack handItem = event.getItemInHand();
        boolean handItemIsShop = ShopApi.isShopItem(handItem);
        boolean nextToChest = false;
        boolean nextToShop = false;

        for (Chest surroundingChest : ShopUtils.getSurroundingBlockStates(block, Chest.class)) {
            nextToChest = true;

            if (ShopApi.isShop(surroundingChest)) {
                nextToShop = true;
                break;
            }
        }

        if (handItemIsShop) {
            if (nextToShop) {
                player.sendMessage(MessageManager.getCannotPlaceShopNextToShopComponent());
                event.setCancelled(true);
                return;
            } else if (nextToChest) {
                player.sendMessage(MessageManager.getCannotPlaceShopNextToChestComponent());
                event.setCancelled(true);
                return;
            }

            final ShopCreateEvent shopCreateEvent = new ShopCreateEvent(block, chest, player, event.isAsynchronous());

            if (!shopCreateEvent.callEvent()) {
                shopCreateEvent.applyCancelled(event.getPlayer());
                event.setCancelled(true);
                return;
            }

            final ShopInstance shopInstance = ShopApi.getInstance();
            shopInstance.createShop(shopInstance.getDefaultCurrency(), player, new ItemStack(Material.STRUCTURE_VOID), block.getLocation());
        } else {
            if (nextToShop) {
                player.sendMessage(MessageManager.getCannotPlaceChestNextToShopComponent());
                event.setCancelled(true);
            }
        }
    }
}
