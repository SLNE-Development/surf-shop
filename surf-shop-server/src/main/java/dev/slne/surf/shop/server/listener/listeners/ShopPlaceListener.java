package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.util.ShopUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.api.events.state.ShopCreateEvent;
import dev.slne.surf.shop.server.message.MessageManager;

public class ShopPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("java:S3776")
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Chest)) {
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

            final ShopCreateEvent shopCreateEvent = new ShopCreateEvent(block, player);

            if (!shopCreateEvent.callEvent()) {
                shopCreateEvent.applyCancelled(event.getPlayer());
                event.setCancelled(true);
                return;
            }

            ShopApi.getInstance().createShop(player, new ItemStack(Material.STRUCTURE_VOID), block.getLocation());
        } else {
            if (nextToShop) {
                player.sendMessage(MessageManager.getCannotPlaceChestNextToShopComponent());
                event.setCancelled(true);
            }
        }
    }
}
