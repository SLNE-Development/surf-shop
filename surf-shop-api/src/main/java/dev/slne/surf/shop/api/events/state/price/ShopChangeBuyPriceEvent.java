package dev.slne.surf.shop.api.events.state.price;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopChangeBuyPriceEvent extends ShopEvent {

    private double newPrice;

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     */
    public ShopChangeBuyPriceEvent(@NotNull Shop shop, double newPrice, boolean async) {
        super(shop, null, async);
        this.newPrice = newPrice;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }
}
