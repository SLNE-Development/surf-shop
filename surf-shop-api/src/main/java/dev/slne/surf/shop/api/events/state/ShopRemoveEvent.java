package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;

public final class ShopRemoveEvent extends ShopEvent {

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
