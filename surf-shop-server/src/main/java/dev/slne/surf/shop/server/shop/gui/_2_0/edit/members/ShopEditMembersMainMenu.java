package dev.slne.surf.shop.server.shop.gui._2_0.edit.members;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.members.actions.ShopEditMembersAddAnvilMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.members.actions.ShopEditMembersListGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.members.actions.ShopEditMembersRemoveAnvilMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import dev.slne.surf.shop.server.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class ShopEditMembersMainMenu extends ShopGui {


    public ShopEditMembersMainMenu(@NotNull Shop shop, @NotNull SurfShopGui parent, @NotNull HumanEntity clickedHuman) {
        super(shop, parent, 3, Component.text("Mitglieder Verwalten", MessageManager.PRIMARY));

        final StaticPane pane = new StaticPane(0, 1, 9, 1);

        // mitglieder hinzufügen (rechts)
        // mitglieder entfernen (links)
        // mitglieder anzeigen (mitte)

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_MEMBERS_ADD)) {
            pane.addItem(addMemberItem(), 7, 0);
        }

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_MEMBERS_REMOVE)) {
            pane.addItem(removeMemberItem(), 1, 0);
        }

        if (clickedHuman.hasPermission(Permissions.SHOP_MENU_EDIT_MEMBERS_LIST)) {
            pane.addItem(listMembersItem(), 4, 0);
        }


        addPane(pane);
    }

    private GuiItem addMemberItem() {
        return new GuiItem(ItemUtils.addMemberItem(), event -> new ShopEditMembersAddAnvilMenu(this, getShop()).show(event.getWhoClicked()));
    }

    private GuiItem removeMemberItem() {
        return new GuiItem(ItemUtils.removeMemberItem(), event -> new ShopEditMembersRemoveAnvilMenu(this, getShop()).show(event.getWhoClicked()));
    }

    private GuiItem listMembersItem() {
        return new GuiItem(ItemUtils.listMembersItem(), event -> new ShopEditMembersListGui(getShop(), this).show(event.getWhoClicked()));
    }
}
