package dev.slne.shop.listener.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.shop.Shop;

public class ShopHopperListener implements Listener {

    @EventHandler
    public void onHopperItemMove(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        Chest sourceChest = getChest(source);
        Chest destinationChest = getChest(destination);

        if (sourceChest != null && handleChest(sourceChest)) {
            event.setCancelled(true);
        }

        if (destinationChest != null && handleChest(destinationChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("java:S2583")
    public void onHopperPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (!block.getType().equals(Material.HOPPER)) {
            return;
        }

        if (aroundIsShop(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperPistonRetract(BlockPistonRetractEvent event) {
        event.setCancelled(handlePiston(event.getBlocks(), event.getDirection()));
    }

    @EventHandler
    public void onHopperPistonExtend(BlockPistonExtendEvent event) {
        event.setCancelled(handlePiston(event.getBlocks(), event.getDirection()));
    }

    /**
     * Handles the piston
     *
     * @param blocks        the blocks
     * @param moveDirection the move direction
     * @return true if the piston is a shop
     */
    private boolean handlePiston(List<Block> blocks, BlockFace moveDirection) {
        if (!containsHopper(blocks)) {
            return false;
        }

        return moveBlocksInDirection(moveDirection, blocks).stream()
                .anyMatch(this::aroundIsShop);
    }

    /**
     * Simulates the blocks in the direction
     *
     * @param moveDirection the move direction
     * @param blocks        the blocks
     * @return the moved blocks
     */
    private List<Block> moveBlocksInDirection(BlockFace moveDirection, List<Block> blocks) {
        List<Block> movedBlocks = new ArrayList<>();
        Map<BlockFace, int[]> blockFaceMap = Map.of(BlockFace.NORTH, new int[] { 0, 0, -1 },
                BlockFace.EAST, new int[] { 1, 0, 0 }, BlockFace.SOUTH, new int[] { 0, 0, 1 },
                BlockFace.WEST, new int[] { -1, 0, 0 }, BlockFace.UP, new int[] { 0, 1, 0 },
                BlockFace.DOWN, new int[] { 0, -1, 0 });

        for (Block block : blocks) {
            Location location = block.getLocation();
            int[] move = blockFaceMap.get(moveDirection);

            if (move == null) {
                continue;
            }

            Location newLocation = location.clone().add(move[0], move[1], move[2]);
            movedBlocks.add(location.getWorld().getBlockAt(newLocation));
        }

        return movedBlocks;
    }

    /**
     * Checks if the blocks contains a hopper
     *
     * @param blocks the blocks
     * @return true if the blocks contains a hopper
     */
    private boolean containsHopper(List<Block> blocks) {
        return blocks.stream().anyMatch(block -> block.getType().equals(Material.HOPPER));
    }

    /**
     * Checks if a block next to the block is a shop
     *
     * @param block the block
     * @return true if the block is a shop
     */
    private boolean aroundIsShop(Block block) {
        BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN };

        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);

            if (relative.getType().equals(Material.CHEST)) {
                Chest chest = (Chest) relative.getState();

                if (handleChest(chest)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the chest from the inventory
     *
     * @param inventory the inventory
     * @return the chest
     */
    private Chest getChest(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof Chest chest) {
            return chest;
        }

        return null;
    }

    /**
     * Handles the chest
     *
     * @param chest the chest
     * @return true if the chest is a shop
     */
    private boolean handleChest(Chest chest) {
        PersistentDataContainer container = chest.getPersistentDataContainer();

        return container.has(Shop.SHOP_KEY, PersistentDataType.STRING);
    }

}
