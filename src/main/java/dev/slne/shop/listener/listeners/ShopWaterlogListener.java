package dev.slne.shop.listener.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.shop.Shop;

public class ShopWaterlogListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWaterlog(PlayerBucketEmptyEvent event) {
        if (!(event.getBlock().getState() instanceof Chest chest)) {
            return;
        }

        if (chest.getPersistentDataContainer().has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
