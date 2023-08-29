package dev.slne.surf.shop.server.listener.events.transaction.buy;

import dev.slne.surf.shop.server.listener.events.ShopEvent;
import org.bukkit.entity.Player;

import dev.slne.surf.shop.server.shop.Shop;

public class ShopItemBuyEvent extends ShopEvent {

    /**
     * Constructs a new shop item buy event.
     *
     * @param shop   The shop.
     * @param player The player.
     */
    public ShopItemBuyEvent(Shop shop, Player player) {
        super(shop, player);
    }

}
