package dev.slne.surf.shop.server.shop.gui._2_0.edit.storage;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.storage.actions.ShopEditStorageAddAnvilMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.storage.actions.ShopEditStorageRemoveAnvilMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import dev.slne.surf.shop.server.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopEditStorageMainMenu extends ShopGui {
    public ShopEditStorageMainMenu(@NotNull Shop shop, @Nullable SurfShopGui parent, HumanEntity clickedHuman) {
        super(shop, parent, 3, Component.text("Lager", MessageManager.PRIMARY));

        // items hinzufügen (rechts)
        // items entfernen (links)
        // aktuelle items anzeigen (mitte)

        final StaticPane pane = new StaticPane(0, 1, 9, 1);

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_STORAGE_ADD)) {
            pane.addItem(addStorageItem(), 7, 0);
        }

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_STORAGE_REMOVE)) {
            pane.addItem(removeStorageItem(), 1, 0);
        }

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_STORAGE_LIST)) {
            pane.addItem(listStorageItem(), 4, 0);
        }

        addPane(pane);
    }

    private GuiItem addStorageItem() {
        return new GuiItem(ItemUtils.addStorageItem(), event -> new ShopEditStorageAddAnvilMenu(this, getShop(), event.getWhoClicked()).show(event.getWhoClicked()));
    }

    private GuiItem removeStorageItem() {
        return new GuiItem(ItemUtils.removeStorageItem(), event -> new ShopEditStorageRemoveAnvilMenu(this, getShop()).show(event.getWhoClicked()));
    }

    private GuiItem listStorageItem() {
        return new GuiItem(ItemUtils.shopStorageInfoItem(getShop()));
    }
}
