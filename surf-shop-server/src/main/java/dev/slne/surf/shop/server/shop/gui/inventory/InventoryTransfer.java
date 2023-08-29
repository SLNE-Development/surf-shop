package dev.slne.surf.shop.server.shop.gui.inventory;

import org.bukkit.inventory.ItemStack;

import dev.slne.surf.shop.server.shop.gui.inventory.exception.ShadowInventoryTransactionException;

public class InventoryTransfer {

    private ShadowInventory from;
    private ShadowInventory to;

    /**
     * Creates a new InventoryTransfer
     *
     * @param from the inventory to transfer from
     * @param to   the inventory to transfer to
     */
    public InventoryTransfer(ShadowInventory from, ShadowInventory to) {
        this.to = to;
        this.from = from;
    }

    /**
     * Finds a non empty slot
     *
     * @param data the item to find
     * @return the slot or -1 if not found
     */
    private int findNonEmpty(ItemStack data) {
        // check for itemstacks
        for (int i = 0; i < to.getSize(); i++) {
            ItemStack item = to.getItem(i);
            if (item != null && item.isSimilar(data) && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }

        // check for empty slots
        for (int i = 0; i < to.getSize(); i++) {
            ItemStack item = to.getItem(i);
            if (item == null || item.getType().isAir()) {
                return i;
            }
        }

        return -1;
    }

    public enum InventoryResultReason {
        /**
         * The from inventory is empty
         */
        FROM_IS_EMPTY,

        /**
         * The to inventory is full
         */
        TO_IS_FULL,

        /**
         * The number of items to transfer has been reached
         */
        N_REACHED
    }

    public class InventoryTransferResult {
        private int leftOver;
        private int itemsMoved;
        private InventoryResultReason reason;

        /**
         * Gets the number of items moved
         *
         * @return the number of items moved
         */
        public int getItemsMoved() {
            return itemsMoved;
        }

        /**
         * Gets the number of items left over
         *
         * @return the number of items left over
         */
        public int getLeftOver() {
            return leftOver;
        }

        /**
         * Gets the reason
         *
         * @return the reason
         */
        public InventoryResultReason getReason() {
            return reason;
        }

        /**
         * Creates a new InventoryTransferResult
         *
         * @param leftOver   the number of items left over
         * @param itemsMoved the number of items moved
         * @param reason     the reason
         */
        public InventoryTransferResult(int leftOver, int itemsMoved, InventoryResultReason reason) {
            super();

            this.leftOver = leftOver;
            this.itemsMoved = itemsMoved;
            this.reason = reason;
        }
    }

    /**
     * Gets the result
     *
     * @param leftOver the number of items left over
     * @param n        the number of items to transfer
     * @param reason   the reason
     * @return the result
     */
    private InventoryTransferResult getResult(int leftOver, int n, InventoryResultReason reason) {
        int itemsMoved = n - leftOver;

        return new InventoryTransferResult(leftOver, itemsMoved, reason);
    }

    /**
     * Transfers the item
     *
     * @param item the item
     * @param n    the number of items to transfer or -1 for infinity
     * @return an {@link InventoryTransferResult}
     * @throws ShadowInventoryTransactionException
     */
    public InventoryTransferResult transfer(ItemStack data, int n) throws ShadowInventoryTransactionException {
        this.getFrom().startTransaction();
        this.getTo().startTransaction();
        return transferUnattended(data, n);
    }

    /**
     * Transfers the item
     *
     * @param item the item
     * @param n    the number of items to transfer or -1 for infinity
     * @return an {@link InventoryTransferResult}
     */
    @SuppressWarnings({ "java:S3776", "java:S127" })
    public InventoryTransferResult transferUnattended(ItemStack data, int n) {
        int leftOver = n;

        for (int i = 0; i < from.getSize(); i++) {
            ItemStack item = from.getItem(i);

            if (item != null && !item.getType().isAir() && item.isSimilar(data)) {
                int amount = item.getAmount();
                int slotFound = findNonEmpty(data);
                ItemStack found = null;

                if (slotFound >= 0) {
                    found = to.getItem(slotFound);
                    if (found != null && found.getType().isAir()) {
                        // Dont know whats about this new feature?
                        found = null;
                    }
                } else {
                    return getResult(leftOver, n, InventoryResultReason.TO_IS_FULL);
                }

                int foundAmount = found != null ? found.getAmount() : 0;
                int diff = Math.min(data.getMaxStackSize() - foundAmount, amount);

                if (diff == 0) {
                    continue;
                }

                if (leftOver > -1 && leftOver - diff <= 0) {
                    diff = leftOver;
                    if (diff <= 0) {
                        return getResult(leftOver, n, InventoryResultReason.N_REACHED);
                    }
                }

                leftOver -= diff;

                amount -= diff;
                foundAmount += diff;

                if (found != null) {
                    found.setAmount(foundAmount);
                }

                if (foundAmount <= 0) {
                    found = null;
                }

                if (found == null && foundAmount > 0) {
                    // not that when items amount is 0
                    // type converts to AIR
                    found = item.clone();
                    found.setAmount(foundAmount);
                }

                item.setAmount(amount);

                from.setItem(i, item);
                to.setItem(slotFound, found);

                // for the use of infinite shadow inventories
                item = from.getItem(i);
                if (item.getAmount() > 0) {
                    i--;
                }
            }
        }

        return getResult(leftOver, n,
                leftOver > 0 ? InventoryResultReason.FROM_IS_EMPTY : InventoryResultReason.N_REACHED);
    }

    /**
     * Gets the to inventory
     *
     * @return the to inventory
     */
    public ShadowInventory getTo() {
        return to;
    }

    /**
     * Gets the from inventory
     *
     * @return the from inventory
     */
    public ShadowInventory getFrom() {
        return from;
    }

    /**
     * Performs the actions to real inventories
     *
     * @throws ShadowInventoryTransactionException
     */
    public void startTransaction() throws ShadowInventoryTransactionException {
        getFrom().startTransaction();
        getTo().startTransaction();
    }

    /**
     * Performs the actions to real inventories
     *
     * @throws ShadowInventoryTransactionException
     */
    public void performTransaction() throws ShadowInventoryTransactionException {
        getFrom().performTransaction();
        getTo().performTransaction();
    }

    /**
     * Reverts the actions
     *
     * @throws ShadowInventoryTransactionException
     */
    public void rollbackTransaction() throws ShadowInventoryTransactionException {
        getFrom().rollbackTransaction();
        getTo().rollbackTransaction();
    }
}
