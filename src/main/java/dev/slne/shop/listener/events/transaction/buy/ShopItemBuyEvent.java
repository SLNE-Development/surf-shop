package dev.slne.shop.listener.events.transaction.buy;

import org.bukkit.entity.Player;

import dev.slne.shop.listener.events.ShopEvent;
import dev.slne.shop.shop.Shop;

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
