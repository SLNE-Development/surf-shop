package dev.slne.surf.shop.api.events.transaction.buy;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;

public final class ShopItemBuyEvent extends ShopEvent {

    private double price;

    /**
     * Constructs a new shop item buy event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    public ShopItemBuyEvent(Shop shop, Player player, double price) {
        super(shop, player);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
