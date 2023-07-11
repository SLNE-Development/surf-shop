package dev.slne.shop.listener.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.shop.Shop;

public class ShopExplodeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::handleExplode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::handleExplode);
    }

    /**
     * Handles the block being exploded
     *
     * @param block the block
     * @return true if the block is a shop
     */
    private boolean handleExplode(Block block) {
        if (!block.getType().equals(Material.CHEST)) {
            return false;
        }

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();

        return container.has(Shop.SHOP_KEY, PersistentDataType.STRING);
    }

}
