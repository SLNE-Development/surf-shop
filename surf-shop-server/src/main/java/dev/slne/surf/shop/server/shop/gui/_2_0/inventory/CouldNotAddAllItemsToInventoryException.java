package dev.slne.surf.shop.server.shop.gui._2_0.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CouldNotAddAllItemsToInventoryException extends RuntimeException {

    private final Map<Integer, ItemStack> leftOver;

    public CouldNotAddAllItemsToInventoryException(Map<Integer, ItemStack> leftOver) {
        super("Could not add all items to inventory");

        this.leftOver = leftOver;
    }

    public Map<Integer, ItemStack> getLeftOver() {
        return leftOver;
    }
}
