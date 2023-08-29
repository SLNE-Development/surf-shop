package dev.slne.surf.shop.server.shop.gui.inventory.bukkit;

import org.bukkit.inventory.ItemStack;

import dev.slne.surf.shop.server.shop.gui.inventory.ShadowInventory;
import dev.slne.surf.shop.server.shop.gui.inventory.exception.ShadowInventoryTransactionException;

public class InfiniteShadowInventory implements ShadowInventory {

    private ItemStack item;
    private boolean inTransaction;

    /**
     * Creates a new InfiniteShadowInventory
     *
     * @param item the item to be returned
     */
    public InfiniteShadowInventory(ItemStack item) {
        if (item != null) {
            this.item = item.clone();
            this.item.setAmount(this.item.getMaxStackSize());
        }
    }

    @Override
    public ItemStack getItem(int index) {
        if (item == null) {
            return null;
        }

        return item.clone();
    }

    @Override
    public void setItem(int index, ItemStack item) {
        // Do nothing
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void addItem(ItemStack itemStack) {
        // Do nothing
    }

    @Override
    @SuppressWarnings("java:S4144")
    public void performTransaction() throws ShadowInventoryTransactionException {
        if (!inTransaction) {
            throw new ShadowInventoryTransactionException("No transaction started");
        }

        this.inTransaction = false;
    }

    @Override
    @SuppressWarnings("java:S4144")
    public void startTransaction() throws ShadowInventoryTransactionException {
        if (inTransaction) {
            throw new ShadowInventoryTransactionException("Transaction already started");
        }

        this.inTransaction = true;
    }

    @Override
    @SuppressWarnings("java:S4144")
    public void rollbackTransaction() throws ShadowInventoryTransactionException {
        if (!inTransaction) {
            throw new ShadowInventoryTransactionException("No transaction started");
        }

        this.inTransaction = false;
    }
}
