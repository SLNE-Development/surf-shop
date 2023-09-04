package dev.slne.surf.shop.api.events.transaction.item;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import org.jetbrains.annotations.NotNull;

public class ShopItemTransactionAddedEvent extends CancellableShopEvent {

    private final ShopTransaction transaction;

    /**
     * Creates a new {@link ShopItemTransactionAddedEvent} instance
     *
     * @param shop        the shop
     * @param transaction the transaction
     */
    public ShopItemTransactionAddedEvent(@NotNull Shop shop, ShopTransaction transaction) {
        super(shop, transaction.getTransactionSender(), false);

        this.transaction = transaction;
    }

    /**
     * Returns the transaction
     *
     * @return the transaction
     */
    public ShopTransaction getTransaction() {
        return transaction;
    }
}
