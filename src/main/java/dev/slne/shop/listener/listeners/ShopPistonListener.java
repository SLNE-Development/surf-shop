package dev.slne.shop.listener.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.shop.Shop;

public class ShopPistonListener implements Listener {

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        boolean hasShop = false;

        for (Block block : event.getBlocks()) {
            if (handlePiston(block)) {
                hasShop = true;
            }
        }

        if (hasShop) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        boolean hasShop = false;

        for (Block block : event.getBlocks()) {
            if (handlePiston(block)) {
                hasShop = true;
            }
        }

        if (hasShop) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles the piston moving the block
     *
     * @param block the block
     * @return true if the block is a shop
     */
    private boolean handlePiston(Block block) {
        if (!block.getType().equals(Material.CHEST)) {
            return false;
        }

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();

        return container.has(Shop.SHOP_KEY, PersistentDataType.STRING);
    }
}
