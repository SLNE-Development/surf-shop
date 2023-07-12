package dev.slne.shop.shop.gui.inventory.bukkit;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.slne.shop.shop.gui.inventory.ShadowInventory;
import dev.slne.shop.shop.gui.inventory.exception.InventorySizeException;
import dev.slne.shop.shop.gui.inventory.exception.ShadowInventoryTransactionException;

public class LimitedShadowInventory implements ShadowInventory {

    private ItemStack[] storage;
    private Inventory inventory;
    private DiffItemStack[] changes;
    private boolean inTransaction;

    private class DiffItemStack {
        private ItemStack item;

        /**
         * Creates a new DiffItemStack
         *
         * @param item the item
         */
        public DiffItemStack(ItemStack item) {
            this.item = item;
        }
    }

    /**
     * Creates a new LimitedShadowInventory
     *
     * @param items the items to be shadowed
     */
    public LimitedShadowInventory(ItemStack[] items) {
        this.storage = items;
        this.changes = new DiffItemStack[this.storage.length];
    }

    /**
     * Creates a new LimitedShadowInventory
     *
     * @param inv the inventory to be shadowed
     */
    public LimitedShadowInventory(Inventory inventory) {
        this.inventory = inventory;
        this.storage = inventory.getStorageContents();
        this.changes = new DiffItemStack[this.storage.length];
    }

    @Override
    public ItemStack getItem(int index) {
        ItemStack orig = storage[index];
        DiffItemStack change = changes[index];

        if (change != null) {
            if (!inTransaction) {
                throw new ShadowInventoryTransactionException("Didnt expect to have changes while not transacting");
            }

            return change.item;
        }

        return orig != null ? orig.clone() : null;
    }

    @Override
    public void setItem(int index, ItemStack itemStack) {
        if (!inTransaction) {
            storage[index] = itemStack;
            inventory.setItem(index, itemStack);
            return;
        }

        changes[index] = new DiffItemStack(itemStack != null ? itemStack.clone() : null);
    }

    @Override
    public int getSize() {
        return storage.length;
    }

    /**
     * Returns the bukkit inventory of this {@link LimitedShadowInventory}
     *
     * @return the bukkit {@link Inventory}
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets the inventory
     *
     * @param inventory the bukkit inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public void performTransaction() throws ShadowInventoryTransactionException {
        if (!inTransaction) {
            throw new ShadowInventoryTransactionException("No transaction started");
        }

        if (inventory == null) {
            return;
        }

        // only throw error when our storage is greater than target
        if (inventory.getSize() < storage.length) {
            throw new InventorySizeException("Inventory size doesnt equal storage, expected: " + storage.length
                    + ", but got: " + inventory.getSize());
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= storage.length) {
                // at this point the target has slots, that should be ignored
                break;
            }

            ItemStack item = inventory.getItem(i);
            if (item == null ? item == storage[i] : item.isSimilar(storage[i])) {
                DiffItemStack diff = changes[i];

                if (diff != null) {
                    inventory.setItem(i, diff.item);
                    storage[i] = diff.item;
                    changes[i] = null;
                }
            }
        }
        
        inTransaction = false;
    }

    @Override
    public void startTransaction() throws ShadowInventoryTransactionException {
        if (inTransaction) {
            throw new ShadowInventoryTransactionException("Transaction already started");
        }

        this.inTransaction = true;
    }

    @Override
    public void rollbackTransaction() throws ShadowInventoryTransactionException {
        if (!inTransaction) {
            throw new ShadowInventoryTransactionException("No transaction started");
        }

        for (int i = 0; i < changes.length; i++) {

            changes[i] = null;
        }

        this.inTransaction = false;
    }

    @Override
    public void addItem(ItemStack itemStack) {
        throw new UnsupportedOperationException("Cannot add item in ShadowInventories");
    }
}
