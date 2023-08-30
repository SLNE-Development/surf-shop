package dev.slne.surf.shop.api.events.transaction.buy;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;

public final class ShopItemBuyEvent extends ShopEvent {

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
