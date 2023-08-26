package dev.slne.shop.shop.gui._2_0;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui._2_0.edit.ShopEditMainMenu;
import dev.slne.shop.shop.gui.sell.ShopSellMenu;
import dev.slne.shop.shop.gui.utils.ItemUtils;
import dev.slne.shop.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopMainMenu extends ShopGui {
    private final Player viewingPlayer;

    public ShopMainMenu(@NotNull Shop shop, @NotNull Player viewingPlayer) {
        super(shop, null, 6, Component.text("Shop - Menü", MessageManager.PRIMARY));
        this.viewingPlayer = viewingPlayer;

        final StaticPane shopPane = new StaticPane(0, 1, 9, 4);

        if (viewingPlayer.hasPermission(Permissions.MENU_SELL)) {
            shopPane.addItem(sellItem(), 1, 2);

            if (shop.getItemStack() == null || shop.getAmount() == 0) {
                shopPane.addItem(new GuiItem(ItemUtils.disabledItem()), 1, 3);
            }
        }

        if (viewingPlayer.hasPermission(Permissions.MENU_OWNER)) {
            shopPane.addItem(new GuiItem(ItemUtils.head(shop.getOwnerUuid())), 4, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.MENU_EDIT) && (shop.isOwner(viewingPlayer) || shop.isMember(viewingPlayer))) {
            shopPane.addItem(editShopItem(), 4, 1);
        }

        if (viewingPlayer.hasPermission(Permissions.MENU_INFO)) {
            shopPane.addItem(infoItem(), 4, 2);
        }

        if (viewingPlayer.hasPermission(Permissions.MENU_SHOP_ITEM)) {
            shopPane.addItem(shopItem(), 4, 3);
        }

        if (viewingPlayer.hasPermission(Permissions.MENU_BUY)) {
            shopPane.addItem(new GuiItem(ItemUtils.buyItem()), 7, 2);
            shopPane.addItem(new GuiItem(ItemUtils.disabledItem()), 7, 3);
        }

    }

    private GuiItem sellItem() {
        return new GuiItem(ItemUtils.sellItem(getShop()), event -> {
            if (getShop().getItemStack() != null && getShop().getAmount() > 0) {
                new ShopSellMenu(this, getShop(), viewingPlayer).show(event.getWhoClicked());
            }
        });
    }

    private GuiItem editShopItem() {
        return new GuiItem(ItemUtils.editShopItem(), event -> new ShopEditMainMenu(getShop(), this, viewingPlayer).show(event.getWhoClicked()));
    }

    private GuiItem infoItem() {
        return new GuiItem(ItemUtils.infoItem(getShop()));
    }

    private GuiItem shopItem() {
        return new GuiItem(ItemUtils.shopItem(getShop()));
    }
}
