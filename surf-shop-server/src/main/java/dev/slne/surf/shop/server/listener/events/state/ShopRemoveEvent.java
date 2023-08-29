package dev.slne.surf.shop.server.listener.events.state;

import dev.slne.surf.shop.server.listener.events.ShopEvent;
import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.entity.Player;

public class ShopRemoveEvent extends ShopEvent {

    /**
     * Constructs a new shop remove event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    public ShopRemoveEvent(ServerShop shop, Player player) {
        super(shop, player);
    }

}
