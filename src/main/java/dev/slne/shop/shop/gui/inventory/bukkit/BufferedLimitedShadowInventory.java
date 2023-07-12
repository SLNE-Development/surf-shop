package dev.slne.shop.shop.gui.inventory.bukkit;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.slne.shop.shop.gui.inventory.exception.ShadowInventoryTransactionException;

public class BufferedLimitedShadowInventory extends LimitedShadowInventory {

    /**
     * Creates a new BufferedLimitedShadowInventory
     *
     * @param inv the inventory to be shadowed
     */
    public BufferedLimitedShadowInventory(Inventory inventory) {
        super(inventory);

        try {
            this.startTransaction();
        } catch (ShadowInventoryTransactionException exception) {
            // Dont print
        }
    }

    /**
     * Creates a new BufferedLimitedShadowInventory
     *
     * @param stack the items to be shadowed
     */
    public BufferedLimitedShadowInventory(ItemStack[] stack) {
        super(stack);

        try {
            this.startTransaction();
        } catch (ShadowInventoryTransactionException exception) {
            // Dont print
        }
    }

    @Override
    public void setInventory(Inventory inventory) {
        super.setInventory(inventory);

        try {
            this.performTransaction();
        } catch (ShadowInventoryTransactionException e) {
            e.printStackTrace();
        }
    }
}
