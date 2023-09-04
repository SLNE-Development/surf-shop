package dev.slne.surf.shop.server.shop.gui._2_0.sell;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.confirmation.ConfirmationGui;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiSound;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiUtils;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import dev.slne.surf.shop.server.util.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShopSellMenu extends ShopGui {

    private final Player viewingPlayer;
    private StaticPane shopPane;
    private int selectedAmount;

    public ShopSellMenu(@NotNull Shop shop, @NotNull SurfShopGui parent, Player viewingPlayer) {
        super(shop, parent, 6, Component.text("Shop - Kaufen"));

        this.viewingPlayer = viewingPlayer;
        this.selectedAmount = 0;

        shopPane = new StaticPane(0, 1, 9, 4);

        setIncreaseDecreaseItems(viewingPlayer);

        // X == 4
        if (viewingPlayer.hasPermission(Permissions.SELL_MENU_OWNER)) {
            shopPane.addItem(new GuiItem(ItemUtils.head(shop.getOwnerUUID())), 4, 0);
        }

        if (viewingPlayer.hasPermission(Permissions.SELL_MENU_INFO)) {
            shopPane.addItem(new GuiItem(ItemUtils.infoItem(shop)), 4, 1);
        }

        if (viewingPlayer.hasPermission(Permissions.SELL_MENU_SHOP_ITEM)) {
            shopPane.addItem(new GuiItem(ItemUtils.shopItem(shop)), 4, 2);
        }

        addPane(shopPane);
    }


    /**
     * Sets the increase/decrease items
     *
     * @param viewingPlayer the player viewing the shop
     */
    @SuppressWarnings("java:S3776")
    private void setIncreaseDecreaseItems(Player viewingPlayer) {
        int maxAmount = getShop().amount();

        // X == 0
        if (viewingPlayer.hasPermission(Permissions.DECREASE_1000)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseThousandItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-1000)),
                    0, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseThousandItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-1000)),
                    0, 2);
        }

        // X == 1
        if (viewingPlayer.hasPermission(Permissions.DECREASE_100)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseHundredItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-100)),
                    1, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseHundredItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-100)),
                    1, 2);
        }

        // X == 2
        if (viewingPlayer.hasPermission(Permissions.DECREASE_10)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseTenItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-10)),
                    2, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseTenItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-10)),
                    2, 2);
        }

        // X == 3
        if (viewingPlayer.hasPermission(Permissions.DECREASE_1)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseOneItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-1)),
                    3, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.decreaseOneItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(-1)),
                    3, 2);
        }

        if (viewingPlayer.hasPermission(Permissions.RESET_AMOUNT) && selectedAmount > 0) {
            shopPane.addItem(new GuiItem(ItemUtils.resetCountItem(selectedAmount, maxAmount), event -> resetItems()), 3, 3);
        } else if (viewingPlayer.hasPermission(Permissions.RESET_AMOUNT) && selectedAmount == 0) {
            shopPane.removeItem(3, 3);
        }

        // X == 5
        if (viewingPlayer.hasPermission(Permissions.INCREASE_1)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseOneItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(1)),
                    5, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseOneItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(1)),
                    5, 2);
        }

        if (viewingPlayer.hasPermission(Permissions.SELL_MENU_BUY) && selectedAmount > 0) {
            shopPane.addItem(new GuiItem(ItemUtils.buyItem(selectedAmount, getShop().amount()), event -> buyItems()), 5, 3);
        } else if (viewingPlayer.hasPermission(Permissions.SELL_MENU_BUY) && selectedAmount == 0) {
            shopPane.removeItem(5, 3);
        }

        // X == 6
        if (viewingPlayer.hasPermission(Permissions.INCREASE_10)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseTenItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(10)),
                    6, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseTenItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(10)),
                    6, 2);
        }

        // X == 7
        if (viewingPlayer.hasPermission(Permissions.INCREASE_100)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseHundredItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(100)),
                    7, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseHundredItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(100)),
                    7, 2);
        }

        // X == 8
        if (viewingPlayer.hasPermission(Permissions.INCREASE_1000)) {
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseThousandItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(1000)),
                    8, 1);
            shopPane.addItem(
                    new GuiItem(ItemUtils.increaseThousandItem(selectedAmount, maxAmount),
                            event -> increaseDecreaseAmount(1000)),
                    8, 2);
        }
    }

    /**
     * Increases or decreases the amount of items to be bought.
     *
     * @param amount the amount to increase or decrease by
     */
    private void increaseDecreaseAmount(int amount) {
        this.selectedAmount += amount;
        int maxAmount = getShop().amount();

        if (this.selectedAmount <= 0) {
            this.selectedAmount = 0;
        } else if (this.selectedAmount > maxAmount) {
            this.selectedAmount = maxAmount;
        }

        setIncreaseDecreaseItems(viewingPlayer);
        update();
    }

    /**
     * Buys the selected amount of items.
     */
    private void buyItems() {
        final ItemStack itemStack = getShop().item();

        if (itemStack == null) {
            return;
        }

        Component displayName = (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
                ? itemStack.getItemMeta().displayName()
                : Component.text(itemStack.getType().name()));

        assert displayName != null;
        displayName = displayName.colorIfAbsent(MessageManager.VARIABLE_VALUE);


        final List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Bist du dir sicher, dass du", NamedTextColor.GRAY));
        lore.add(Component.text(selectedAmount * getShop().quantity() + "x", MessageManager.VARIABLE_VALUE).append(Component.text(" "))
                .append(displayName));
        lore.add(Component.text("kaufen möchtest?", NamedTextColor.GRAY));

        final ConfirmationGui confirmationGui = new ConfirmationGui(
                this,
                null,
                null,
                displayName.append(Component.text(" kaufen", NamedTextColor.GOLD)),
                lore.toArray(Component[]::new)
        );

        confirmationGui.setOnConfirm(event -> handleBuyConfirm(confirmationGui));
        confirmationGui.setOnClose(event -> getShop().unlock());
        confirmationGui.show(viewingPlayer);
    }

    /**
     * Resets the items.
     */
    private void resetItems() {
        this.selectedAmount = 0;
        setIncreaseDecreaseItems(viewingPlayer);
        update();
    }

    /**
     * Handles the sell confirm.
     *
     * @param gui the gui
     */
    private void handleBuyConfirm(ConfirmationGui gui) {
        if (getShop().locked() && !getShop().getLockedBy().map(player -> player.equals(viewingPlayer)).orElse(false)) {
            viewingPlayer.sendMessage(MessageManager.getShopIsLockedComponent(null));
            GuiUtils.playGuiSound(GuiSound.DENY_ACTION, viewingPlayer);
            gui.backToParent(viewingPlayer);
            return;
        }

        getShop().lock(viewingPlayer);
        getShop().sell(viewingPlayer, selectedAmount).thenAcceptAsync(success -> {
            if (!success) {
                viewingPlayer.sendMessage(MessageManager.getErrorComponent());
                gui.backToParent(viewingPlayer);
            }

            getShop().unlock();
        });
    }
}
