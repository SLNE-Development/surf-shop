package dev.slne.surf.shop.server.listener.events.state;

import dev.slne.surf.shop.server.listener.events.ShopEvent;
import org.bukkit.entity.Player;

import dev.slne.surf.shop.server.shop.Shop;

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
