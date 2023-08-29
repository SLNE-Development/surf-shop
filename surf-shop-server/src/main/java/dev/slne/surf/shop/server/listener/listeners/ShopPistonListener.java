package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.persistence.PersistentDataType;

public class ShopPistonListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
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

    @EventHandler(priority = EventPriority.LOWEST)
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
        return block.getState() instanceof Chest chest && chest.getPersistentDataContainer().has(ServerShop.SHOP_KEY, PersistentDataType.STRING);
    }
}
