package dev.slne.surf.shop.api.shop.transaction;

import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ShopTransaction {

    /**
     * Get the shop that this transaction is for.
     *
     * @return the shop
     */
    Shop getShop();

    /**
     * Get the amount of the transaction.
     *
     * @return the amount
     */
    int getAmount();

    /**
     * Get the sender of the transaction.
     *
     * @return the sender
     */
    UUID getTransactionSenderUuid();

    /**
     * Get the sender of the transaction.
     *
     * @return the sender
     */
    default OfflinePlayer getTransactionSender() {
        return Bukkit.getOfflinePlayer(getTransactionSenderUuid());
    }

    /**
     * Get the id of the transaction.
     *
     * @return the id
     */
    UUID getTransactionId();

    /**
     * Executes the transaction.
     *
     * @return a {@link CompletableFuture} that completes with the {@link ShopTransactionResult}.
     */
    CompletableFuture<ShopTransactionResult> create();

}
