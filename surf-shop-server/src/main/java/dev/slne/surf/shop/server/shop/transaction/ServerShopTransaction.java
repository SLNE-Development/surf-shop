package dev.slne.surf.shop.server.shop.transaction;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.events.transaction.item.ShopItemTransactionAddedEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.spring.repository.jpa.ShopTransactionRepository;
import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.*;

@Entity(name = "ShopTransaction")
@Table(name = "shop_transactions")
public class ServerShopTransaction implements ShopTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long id = -1L;

    @Column(name = "transaction_id", nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID transactionId;

    @Column(name = "transaction_sender")
    @JdbcTypeCode(SqlTypes.CHAR)
    @Nullable
    private UUID transactionSender;

    @Column(name = "transaction_amount", nullable = false, length = 11)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private int transactionAmount = 0;

    @Lob
    @Column(name = "reason")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Nullable
    private String reason;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shop_id")
    private ServerShop shop;

    protected ServerShopTransaction() {
        // JPA
    }

    /**
     * Creates a new shop transaction
     *
     * @param shop              the shop
     * @param transactionSender the transaction sender
     * @param amount            the amount
     */
    public ServerShopTransaction(Shop shop, @Nullable UUID transactionSender, int amount, @Nullable String reason) {
        this.transactionAmount = amount;
        this.transactionSender = transactionSender;
        this.transactionId = UUID.randomUUID();
        this.reason = reason;
    }

    public ServerShopTransaction(Shop shop, @Nullable UUID transactionSender, int amount) {
        this(shop, transactionSender, amount, null);
    }

    @Override
    public Shop getShop() {
        return shop;
    }

    @Override
    public int getTransactionAmount() {
        checkState(this.transactionAmount >= 0, "transaction amount cannot be negative");

        return this.transactionAmount;
    }

    /**
     * Set the amount of the transaction.
     *
     * @param amount the amount
     * @return this transaction
     */
    @Override
    public ShopTransaction setTransactionAmount(int amount) {
        checkArgument(amount >= 0, "transaction amount cannot be negative");

        this.transactionAmount = amount;
        return this;
    }

    @Override
    public Optional<UUID> getTransactionSenderUuid() {
        return Optional.ofNullable(this.transactionSender);
    }

    /**
     * Set the sender of the transaction.
     *
     * @param uuid the sender
     * @return this transaction
     */
    @Override
    public ShopTransaction setTransactionSenderUuid(@Nullable UUID uuid) {
        this.transactionSender = uuid;
        return this;
    }

    /**
     * Get the sender of the transaction.
     *
     * @return the sender
     */
    @Override
    public Optional<OfflinePlayer> getTransactionSender() {
        return getTransactionSenderUuid().map(Bukkit::getOfflinePlayer);
    }

    /**
     * Gets the reason for the transaction.
     *
     * @return the reason
     */
    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason);
    }

    /**
     * Sets the reason for the transaction.
     *
     * @param reason the reason
     * @return this transaction
     */
    @Override
    public ShopTransaction setReason(@Nullable String reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public UUID getTransactionId() {
        return this.transactionId;
    }

    @Override
    public CompletableFuture<ShopTransactionResult> execute() {
        if (getShop().amount() + getTransactionAmount() < 0) {
            return CompletableFuture.completedFuture(ShopTransactionResult.FAILED_INSUFFICIENT_FUNDS);
        }

        final ShopItemTransactionAddedEvent event = new ShopItemTransactionAddedEvent(getShop(), this, !Bukkit.isPrimaryThread());

        if (!event.callEvent()) {
            return CompletableFuture.completedFuture(ShopTransactionResult.CANCELLED);
        }

        return CompletableFuture.supplyAsync(() -> {
                    ShopApi.getContext().getBean(ShopTransactionRepository.class).save(this);
                    return ShopTransactionResult.SUCCESS;
                })
                .exceptionally(throwable -> {
                    DataApi.getDataInstance().logError(getClass(), "Failed to execute shop transaction", throwable);
                    return ShopTransactionResult.FAILED;
                });
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerShopTransaction that)) return false;

        if (getTransactionId() != null ? !getTransactionId().equals(that.getTransactionId()) : that.getTransactionId() != null)
            return false;
        return getTransactionSender().equals(that.getTransactionSender());
    }

    @Override
    public int hashCode() {
        int result = getTransactionId() != null ? getTransactionId().hashCode() : 0;
        result = 31 * result + getTransactionSender().hashCode();
        return result;
    }

    @Override
    public ShopTransaction inter() {
        return this;
    }
}
