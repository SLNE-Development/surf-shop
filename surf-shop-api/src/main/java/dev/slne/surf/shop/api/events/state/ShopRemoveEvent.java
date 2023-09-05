package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;

public final class ShopRemoveEvent extends CancellableShopEvent {

    /**
     * Constructs a new shop remove event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    public ShopRemoveEvent(Shop shop, Player player, boolean async) {
        super(shop, player, async);
    }

}
