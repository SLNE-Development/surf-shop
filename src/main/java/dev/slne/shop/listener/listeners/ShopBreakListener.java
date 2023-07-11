package dev.slne.shop.listener.listeners;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.shop.Shop;

public class ShopBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Material type = block.getType();

        if (!type.equals(Material.CHEST)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();

        if (!container.has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
            return;
        }

        String shopUuidString = container.get(Shop.SHOP_KEY, PersistentDataType.STRING);
        UUID shopUuid = UUID.fromString(shopUuidString);

        Shop shop = BukkitApi.getInstance().getShopManager().getShop(shopUuid);

        if (shop == null) {
            // There was an error in deleting your shop. Please contact an administrator.
            event.setCancelled(true);
            return;
        }

        UUID ownerUuid = shop.getOwnerUuid();
        if (ownerUuid != null && !ownerUuid.equals(player.getUniqueId())) {
            // You do not own this shop
            event.setCancelled(true);
            return;
        }

        shop.delete().thenAcceptAsync(deleted -> {
            if (deleted != null) {
                player.sendMessage("Deleted shop");

                BukkitApi.getInstance().getShopManager().getVisualizerTask().removeVisualizer(shop);
                BukkitApi.getInstance().getShopManager().removeShop(shop);
            } else {
                // There was an error in deleting your shop. Please contact an administrator.
                event.setCancelled(true);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            // There was an error in deleting your shop. Please contact an administrator.
            event.setCancelled(true);
            return null;
        });
    }

}
