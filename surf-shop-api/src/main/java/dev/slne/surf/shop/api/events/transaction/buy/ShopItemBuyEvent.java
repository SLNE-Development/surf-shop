package dev.slne.surf.shop.api.events.transaction.buy;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when a shop buys an item from a player.
 */
public final class ShopItemBuyEvent extends CancellableShopEvent {

    private double price;
    private float feeAmountPercentage;

    /**
     * Constructs a new shop item buy event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    @ApiStatus.Internal
    public ShopItemBuyEvent(Shop shop, Player player, double price, float feeAmountPercentage, boolean async) {
        super(shop, player, async);
        this.price = price;
        this.feeAmountPercentage = feeAmountPercentage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the fee amount percentage.
     *
     * @return The fee amount percentage.
     * @see dev.slne.transaction.api.transaction.fee.TransactionFee
     */
    public float getFeeAmountPercentage() {
        return feeAmountPercentage;
    }

    /**
     * Sets the fee amount percentage.
     *
     * @param feeAmountPercentage The fee amount percentage.
     * @see dev.slne.transaction.api.transaction.fee.TransactionFee
     */
    public void setFeeAmountPercentage(float feeAmountPercentage) {
        this.feeAmountPercentage = feeAmountPercentage;
    }
}
