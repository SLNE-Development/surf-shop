package dev.slne.surf.shop.server.shop.gui._2_0.edit;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.EditBuyPriceAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.EditDescriptionAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.EditQuantityAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.EditSellPriceAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.members.ShopEditMembersMainMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.storage.ShopEditStorageMainMenu;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import dev.slne.surf.shop.server.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopEditMainMenu extends ShopGui {
    private final Player viewingPlayer;
    private final StaticPane pane;

    public ShopEditMainMenu(@NotNull Shop shop, @NotNull SurfShopGui parent, @NotNull Player viewingPlayer) {
        super(shop, parent, 4, Component.text("Shop - Bearbeiten", MessageManager.PRIMARY));
        this.viewingPlayer = viewingPlayer;

        this.pane = new StaticPane(0, 1, 9, 4);

        addPane(pane);
        update();
    }

    /**
     * Update the gui for everyone
     */
    @Override
    public void update() {

        // sell / sell price ändern --> über anvil gui eingabe
        // amount ändern
        // Mitglieder (vielleicht direkt im main menu)
        // item ändern (auch mit shift + rechtsklick) --> item entfernen und gui schließen. Kann dann über shift + rechtsklick wieder gesetzt werden
        // lager bearbeiten --> neues gui
        // description line hinzufügen --> über anvil gui eingabe (kostet was?) Max. zwei zeilen mit color codes
        // limits --> neues gui wenn zu voll (global limits (einzelne item anzahl); player limits; spieler blockieren (das gleiche wie player limit auf 0 - brauchst das?); limit reset - vllt. auch automatisch einstellen lassem z.B. nach jedem tag resetten sich die alle limits

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_SELL_PRICE)) {
            pane.addItem(sellPriceItem(), 0, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_BUY_PRICE)) {
            pane.addItem(buyPriceItem(), 1, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_AMOUNT)) {
            pane.addItem(amountItem(), 2, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_MEMBERS)) {
            pane.addItem(membersItem(), 3, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_STORAGE)) {
            pane.addItem(storageItem(), 4, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_DESCRIPTION)) {
            pane.addItem(descriptionItem(), 5, 0);
        }

        /*
        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_GLOBAL_LIMITS)) {
            pane.addItem(globalLimitsItem(), 6, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_PLAYER_LIMITS)) {
            pane.addItem(playerLimitsItem(), 7, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SHOP_EDIT_BLOCK_PLAYER)) {
            pane.addItem(blockPlayerItem(), 8, 0);
        }
         */

        super.update();
    }

    private GuiItem sellPriceItem() {
        return new GuiItem(ItemUtils.editSellPrice(getShop()),
                event -> new EditSellPriceAnvilGui(this, getShop()).show(event.getWhoClicked()));
    }

    private GuiItem buyPriceItem() {
        return new GuiItem(ItemUtils.editBuyPrice(getShop()),
                event -> new EditBuyPriceAnvilGui(this, getShop()).show(event.getWhoClicked()));
    }

    private GuiItem amountItem() {
        Shop shop = getShop();

        assert shop != null;
        return new GuiItem(ItemUtils.editAmount(shop), event -> {
            Bukkit.getScheduler().runTask(BukkitMain.getInstance(), () -> {
                new EditQuantityAnvilGui(ShopEditMainMenu.this, shop).show(event.getWhoClicked());
            });
        });
    }

    private GuiItem membersItem() {
        return new GuiItem(ItemUtils.editMembers(),
                event -> new ShopEditMembersMainMenu(getShop(), this, event.getWhoClicked()).show(
                        event.getWhoClicked()));
    }

    private GuiItem storageItem() {
        return new GuiItem(ItemUtils.editStorage(getShop()),
                event -> new ShopEditStorageMainMenu(getShop(), this, event.getWhoClicked()).show(
                        event.getWhoClicked()));
    }

    private GuiItem descriptionItem() {
        return new GuiItem(ItemUtils.editDescription(getShop()),
                event -> new EditDescriptionAnvilGui(this, getShop()).show(event.getWhoClicked()));
    }

    /*
    private GuiItem globalLimitsItem() {

    }

    private GuiItem playerLimitsItem() {

    }

    private GuiItem blockPlayerItem() {

    }
    */

}
