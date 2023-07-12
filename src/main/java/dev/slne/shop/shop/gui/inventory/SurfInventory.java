package dev.slne.shop.shop.gui.inventory;

import org.bukkit.inventory.ItemStack;

public interface SurfInventory {

    /**
     * Gets the item in the given index
     *
     * @param index the index
     * @return the item
     */
    ItemStack getItem(int index);

    /**
     * Sets the item at the given index
     *
     * @param index     the index
     * @param itemStack the itemstack
     */
    void setItem(int index, ItemStack itemStack);

    /**
     * Adds the given itemstack
     *
     * @param itemStack the item to add
     */
    void addItem(ItemStack itemStack);

    /**
     * Gets the size of this inventory
     *
     * @return
     */
    int getSize();
}
