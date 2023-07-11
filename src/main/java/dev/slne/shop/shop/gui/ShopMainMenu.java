package dev.slne.shop.shop.gui;

import org.bukkit.Material;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopMainMenu extends ShopGui {

    /**
     * Creates a new shop main menu.
     *
     * @param shop the shop
     */
    public ShopMainMenu(Shop shop) {
        super(null, 6, "Shop", shop);

        StaticPane testPane = new StaticPane(0, 1, 9, 4);

        testPane.addItem(
                new GuiItem(ItemUtils.item(Material.ACACIA_BOAT, 1, 0, Component.text("Test", NamedTextColor.GOLD)),
                        event -> new ShopSubTest(this, shop).show(event.getWhoClicked())),
                0, 0);

        addPane(testPane);

        update();
    }

}
