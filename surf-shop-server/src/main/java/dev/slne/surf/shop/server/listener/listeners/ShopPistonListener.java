package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.surf.shop.api.ShopApi;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShopPistonListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event) {
        if (containsShop(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonRetract(@NotNull BlockPistonRetractEvent event) {
        if (containsShop(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    private boolean containsShop(List<Block> blocks) {
        return blocks.stream().anyMatch(ShopApi::isShop);
    }
}
