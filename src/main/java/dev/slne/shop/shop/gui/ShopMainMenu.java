package dev.slne.shop.shop.gui;

import org.bukkit.entity.Player;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.edit.ShopEditMainMenu;
import dev.slne.shop.shop.gui.sell.ShopSellMenu;
import dev.slne.shop.shop.gui.utils.ItemUtils;

public class ShopMainMenu extends ShopGui {

    /**
     * Creates a new shop main menu.
     *
     * @param shop          the shop
     * @param viewingPlayer the player viewing the shop
     */
    public ShopMainMenu(Shop shop, Player viewingPlayer) {
        super(null, 6, "Shop - Menü", shop, viewingPlayer);

        StaticPane shopPane = new StaticPane(0, 0, 9, 6);

        // X == 1
        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.sell")) {
            shopPane.addItem(new GuiItem(ItemUtils.sellItem(shop), event -> {
                if (shop.getItemStack() != null && shop.getAmount() > 0) {
                    new ShopSellMenu(this, shop, viewingPlayer).show(event.getWhoClicked());
                }
            }), 1, 2);

            if (shop.getItemStack() == null || shop.getAmount() == 0) {
                shopPane.addItem(new GuiItem(ItemUtils.disabledItem()), 1, 3);
            }
        }

        // X == 4
        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.owner")) {
            shopPane.addItem(new GuiItem(ItemUtils.head(shop.getOwnerUuid())), 4, 0);
        }

        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.edit")
                && (shop.isOwner(viewingPlayer) || shop.isMember(viewingPlayer))) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.editShopItem(),
                            event -> new ShopEditMainMenu(this, shop, viewingPlayer).show(event.getWhoClicked())),
                    4, 1);
        }

        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.info")) {
            shopPane.addItem(new GuiItem(ItemUtils.infoItem(shop)), 4, 2);
        }

        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.shop-item")) {
            shopPane.addItem(new GuiItem(ItemUtils.shopItem(shop)), 4, 3);
        }

        // X == 7
        if (viewingPlayer.hasPermission("surf.shop.item.main-menu.buy")) {
            shopPane.addItem(new GuiItem(ItemUtils.buyItem()), 7, 2);
            shopPane.addItem(new GuiItem(ItemUtils.disabledItem()), 7, 3);
        }

        addPane(shopPane);
    }

}
