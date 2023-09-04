package dev.slne.surf.shop.server.shop.transaction;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.slne.data.api.DataApi;
import dev.slne.data.api.web.WebRequest;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.events.transaction.item.ShopItemTransactionAddedEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.server.api.API;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerShopTransaction implements ShopTransaction {

    @SerializedName("id")
    private long id;

    @SerializedName("transaction_amount")
    private int amount;

    @SerializedName("transaction_sender")
    private UUID transactionSender;

    @SerializedName("transaction_id")
    private UUID transactionId;

    @SerializedName("shop_uuid")
    private UUID shopUuid;

    @SerializedName("reason")
    private String reason;

    /**
     * Creates a new shop transaction
     *
     * @param shop              the shop
     * @param transactionSender the transaction sender
     * @param amount            the amount
     */
    public ServerShopTransaction(Shop shop, UUID transactionSender, int amount) {
        this.amount = amount;
        this.transactionSender = transactionSender;
        this.transactionId = UUID.randomUUID();
        this.shopUuid = shop.getUUID();
    }

    @Override
    public Shop getShop() {
        return ShopApi.getShop(this.shopUuid);
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public UUID getTransactionSenderUuid() {
        return this.transactionSender;
    }

    @Override
    public UUID getTransactionId() {
        return this.transactionId;
    }

    @Override
    public CompletableFuture<ShopTransactionResult> create() {
        WebRequest request = WebRequest.builder().url(API.SHOP_TRANSACTIONS).json(true).build();

        ShopItemTransactionAddedEvent event = new ShopItemTransactionAddedEvent(getShop(), this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompletableFuture.completedFuture(ShopTransactionResult.CANCELLED);
        }

        return request.executePost().thenApplyAsync(response -> {
            JsonObject dataObject = response.bodyObject(ShopApi.getInstance().getGsonConverter());

            if (dataObject == null) {
                return ShopTransactionResult.FAILED;
            }

            this.id = dataObject.get("id").getAsLong();

            return ShopTransactionResult.SUCCESS;
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop transaction", throwable);
            return ShopTransactionResult.FAILED;
        });
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public long getId() {
        return id;
    }
}
