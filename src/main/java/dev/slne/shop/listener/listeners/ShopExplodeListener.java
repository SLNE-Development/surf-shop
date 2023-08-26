package dev.slne.shop.listener.listeners;

import dev.slne.shop.shop.Shop;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Predicate;

public class ShopExplodeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(handleExplode);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(handleExplode);
    }

    /**
     * Handles the block being exploded
     */
    private static final Predicate<? super Block> handleExplode = block ->
            block.getState() instanceof Chest chest && chest.getPersistentDataContainer().has(Shop.SHOP_KEY, PersistentDataType.STRING);
}
