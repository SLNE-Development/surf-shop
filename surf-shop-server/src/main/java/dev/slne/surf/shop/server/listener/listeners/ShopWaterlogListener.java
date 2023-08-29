package dev.slne.surf.shop.server.listener.listeners;

import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.surf.shop.server.shop.ServerShop;

public class ShopWaterlogListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWaterlog(PlayerBucketEmptyEvent event) {
        if (!(event.getBlock().getState() instanceof Chest chest)) {
            return;
        }

        if (chest.getPersistentDataContainer().has(ServerShop.SHOP_KEY, PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
