package dev.slne.surf.shop.api.events.transaction.item;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class ShopItemTransactionAddedEvent extends CancellableShopEvent {

    private final ShopTransaction transaction;

    /**
     * Creates a new {@link ShopItemTransactionAddedEvent} instance
     *
     * @param shop        the shop
     * @param transaction the transaction
     */
    @ApiStatus.Internal
    public ShopItemTransactionAddedEvent(@NotNull Shop shop, ShopTransaction transaction, boolean async) {
        super(shop, transaction.getTransactionSender().orElse(null), async);

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
