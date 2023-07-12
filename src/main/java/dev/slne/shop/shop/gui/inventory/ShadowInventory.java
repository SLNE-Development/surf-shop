package dev.slne.shop.shop.gui.inventory;

import org.bukkit.inventory.Inventory;

import dev.slne.shop.shop.gui.inventory.exception.ShadowInventoryTransactionException;

public interface ShadowInventory extends SurfInventory {

    /**
     * Starts the transaction
     *
     * @throws ShadowInventoryTransactionException if transaction was already
     *                                             started
     */
    public void startTransaction() throws ShadowInventoryTransactionException;

    /**
     * Performs the actions on underlying {@link Inventory}
     *
     * @throws ShadowInventoryTransactionException if transaction wasnt started
     */
    public void performTransaction() throws ShadowInventoryTransactionException;

    /**
     * Rollbacks the current transaction
     *
     * @throws ShadowInventoryTransactionException if transaction wasnt started
     */
    public void rollbackTransaction() throws ShadowInventoryTransactionException;
}
