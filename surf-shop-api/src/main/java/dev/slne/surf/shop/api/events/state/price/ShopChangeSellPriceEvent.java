package dev.slne.surf.shop.api.events.state.price;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

public class ShopChangeSellPriceEvent extends ShopEvent {
    private double newPrice;

    /**
     * Constructs a new shop event.
     *
     * @param shop     The shop.
     * @param newPrice the new price
     */
    public ShopChangeSellPriceEvent(@NotNull Shop shop, double newPrice, boolean async) {
        super(shop, null, async);
        this.newPrice = newPrice;
    }

    /**
     * Constructs a new shop event.
     *
     * @param shop     The shop.
     * @param newPrice the new price
     */
    public ShopChangeSellPriceEvent(@NotNull Shop shop, double newPrice) {
        this(shop, newPrice, false);
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }
}
