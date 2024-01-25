package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.server.util.ShopUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopHopperListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopperItemMove(InventoryMoveItemEvent event) {
        final Inventory source = event.getSource();
        final Inventory destination = event.getDestination();

        final Chest sourceChest = getChest(source);
        final Chest destinationChest = getChest(destination);

        if (sourceChest != null && ShopApi.isShop(sourceChest)) {
            event.setCancelled(true);
        }

        if (destinationChest != null && ShopApi.isShop(destinationChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopperPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();

        if (!block.getType().equals(Material.HOPPER)) {
            return;
        }

        if (aroundIsShop(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestNextToHopperPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (!block.getType().equals(Material.CHEST)) {
            return;
        }

        if (!ShopApi.isShopItem(event.getItemInHand())) {
            return;
        }

        if (ShopUtils.getSurroundingBlockStates(block, Hopper.class).isEmpty()) { // no hoppers around
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopperPistonRetract(BlockPistonRetractEvent event) {
        event.setCancelled(handlePiston(event.getBlocks(), event.getDirection()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopperPistonExtend(BlockPistonExtendEvent event) {
        event.setCancelled(handlePiston(event.getBlocks(), event.getDirection()));
    }


    // -- HELPER METHODS -- //

    /**
     * Handles the piston
     *
     * @param blocks        the blocks
     * @param moveDirection the move direction
     * @return true if the piston is a shop
     */
    private boolean handlePiston(List<Block> blocks, BlockFace moveDirection) {
        final List<Block> hopperBlocks = blocks.stream()
                .filter(block -> block.getType().equals(Material.HOPPER))
                .toList();

        if (hopperBlocks.isEmpty()) {
            return false;
        }

        return moveBlocksInDirection(moveDirection, hopperBlocks).stream()
                .anyMatch(this::aroundIsShop);
    }

    /**
     * Simulates the blocks in the direction
     *
     * @param moveDirection the move direction
     * @param blocks        the blocks
     * @return the moved blocks
     */
    private @NotNull List<Block> moveBlocksInDirection(BlockFace moveDirection, @NotNull List<Block> blocks) {
        final List<Block> movedBlocks = new ArrayList<>();

        for (Block block : blocks) {
            final Location location = block.getLocation();
            final Location newLocation = location.clone().add(moveDirection.getDirection());
            movedBlocks.add(location.getWorld().getBlockAt(newLocation));
        }

        return movedBlocks;
    }

    /**
     * Checks if a block next to the block is a shop
     *
     * @param block the block
     * @return true if the block is a shop
     */
    private boolean aroundIsShop(Block block) {
        return ShopUtils.getSurroundingBlockStates(block, Chest.class).stream()
                .anyMatch(ShopApi::isShop);
    }

    /**
     * Gets the chest from the inventory
     *
     * @param inventory the inventory
     * @return the chest
     */
    private @Nullable Chest getChest(@NotNull Inventory inventory) {
        final InventoryHolder holder = inventory.getHolder();

        if (holder instanceof Chest chest) {
            return chest;
        }

        return null;
    }
}
