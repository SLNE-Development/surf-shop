package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.surf.shop.server.shop.ServerShop;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopHopperListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopperItemMove(InventoryMoveItemEvent event) {
        final Inventory source = event.getSource();
        final Inventory destination = event.getDestination();

        final Chest sourceChest = getChest(source);
        final Chest destinationChest = getChest(destination);

        if (sourceChest != null && handleChest(sourceChest)) {
            event.setCancelled(true);
        }

        if (destinationChest != null && handleChest(destinationChest)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("java:S2583")
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

        if (!itemIsShopItem(event.getItemInHand())) {
            return;
        }

        if (!aroundIsHopper(block)) {
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

    /**
     * Checks if the item is a shop item
     *
     * @param itemStack the item stack
     * @return true if the item is a shop item
     */
    private boolean itemIsShopItem(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta()
                && itemStack.getItemMeta().getPersistentDataContainer().has(ServerShop.SHOP_KEY, PersistentDataType.STRING);
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
        Map<BlockFace, Vector> blockFaceMap = Map.of(
                BlockFace.NORTH, new Vector(0, 0, -1),
                BlockFace.EAST, new Vector(1, 0, 0),
                BlockFace.SOUTH, new Vector(0, 0, 1),
                BlockFace.WEST, new Vector(-1, 0, 0),
                BlockFace.UP, new Vector(0, 1, 0),
                BlockFace.DOWN, new Vector(0, -1, 0)
        );

        for (Block block : blocks) {
            Location location = block.getLocation();
            Vector move = blockFaceMap.get(moveDirection);

            if (move == null) {
                continue;
            }

            Location newLocation = location.clone().add(move);
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
        for (Chest surroundingChests : ShopUtils.getSurroundingBlockStates(block, Chest.class)) {
            if (handleChest(surroundingChests)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a block next to the block is a hopper
     *
     * @param block the block
     * @return true if the block is a hopper
     */
    private boolean aroundIsHopper(Block block) {
        return !ShopUtils.getSurroundingBlockStates(block, Hopper.class).isEmpty();
    }

    /**
     * Gets the chest from the inventory
     *
     * @param inventory the inventory
     * @return the chest
     */
    private Chest getChest(Inventory inventory) {
        final InventoryHolder holder = inventory.getHolder();

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
    private boolean handleChest(@NotNull Chest chest) {
        return chest.getPersistentDataContainer().has(ServerShop.SHOP_KEY, PersistentDataType.STRING);
    }

}
