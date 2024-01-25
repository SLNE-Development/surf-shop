package dev.slne.surf.shop.api.shop.transaction;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.Interable;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ShopTransaction extends Interable<ShopTransaction> {

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
    @Range(from = 0, to = Integer.MAX_VALUE)
    int getTransactionAmount();

    /**
     * Set the amount of the transaction.
     *
     * @param amount the amount
     * @return this transaction
     */
    ShopTransaction setTransactionAmount(@Range(from = 0, to = Integer.MAX_VALUE) int amount);

    /**
     * Get the sender of the transaction.
     *
     * @return the sender
     */
    Optional<UUID> getTransactionSenderUuid();

    /**
     * Set the sender of the transaction.
     *
     * @param uuid the sender
     * @return this transaction
     */
    ShopTransaction setTransactionSenderUuid(@Nullable UUID uuid);

    /**
     * Get the sender of the transaction.
     *
     * @return the sender
     */
    Optional<OfflinePlayer> getTransactionSender();

    /**
     * Gets the reason for the transaction.
     *
     * @return the reason
     */
    Optional<String> getReason();

    /**
     * Sets the reason for the transaction.
     *
     * @param reason the reason
     * @return this transaction
     */
    ShopTransaction setReason(@Nullable String reason);

    /**
     * Get the id of the transaction.
     *
     * @return the id
     */
    UUID getTransactionId();

    /**
     * Get the id of the transaction.
     *
     * @return the id
     */
    long getId();

    /**
     * Executes the transaction.
     *
     * @return a {@link CompletableFuture} that completes with the {@link ShopTransactionResult}.
     */
    CompletableFuture<ShopTransactionResult> execute();
}
