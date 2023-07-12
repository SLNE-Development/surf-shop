package dev.slne.shop.listener.events.state;

import org.bukkit.entity.Player;

import dev.slne.shop.listener.events.ShopEvent;
import dev.slne.shop.shop.Shop;

public class ShopRemoveEvent extends ShopEvent {

    /**
     * Constructs a new shop remove event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    public ShopRemoveEvent(Shop shop, Player player) {
        super(shop, player);
    }

}
