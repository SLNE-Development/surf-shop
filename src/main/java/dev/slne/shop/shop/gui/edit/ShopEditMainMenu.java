package dev.slne.shop.shop.gui.edit;

import org.bukkit.entity.Player;

import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.ShopGui;

public class ShopEditMainMenu extends ShopGui {

    /**
     * Creates a new shop test menu.
     *
     * @param parent the parent gui
     * @param shop   the shop
     * @param player the player viewing the shop
     */
    public ShopEditMainMenu(ShopGui parent, Shop shop, Player viewingPlayer) {
        super(parent, 2, "Shop - Bearbeiten", shop, viewingPlayer);
    }

}
