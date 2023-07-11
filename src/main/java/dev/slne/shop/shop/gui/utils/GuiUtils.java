package dev.slne.shop.shop.gui.utils;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane.Priority;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.shop.gui.ShopGui;

public class GuiUtils {

    /**
     * Prevents instantiation.
     */
    private GuiUtils() {
    }

    /**
     * Creates an outline pane with the given row.
     *
     * @param row The row of the outline pane.
     * @return The created outline pane.
     */
    public static OutlinePane getOutline(int row) {
        OutlinePane outline = new OutlinePane(0, row, 9, 1);

        outline.setPriority(Priority.LOWEST);
        outline.setRepeat(true);
        outline.addItem(new GuiItem(ItemUtils.paneItem()));

        return outline;
    }

    /**
     * Creates a navigation pane with the given row.
     *
     * @param gui The gui to create the navigation pane for.
     * @param row The row of the navigation pane.
     * @return The created navigation pane.
     */
    public static StaticPane getNavigation(ShopGui gui, int row) {
        StaticPane navigation = new StaticPane(0, row, 9, 1);
        navigation.setPriority(Priority.HIGHEST);

        int closeX = gui.hasParent() ? 5 : 4;

        if (gui.hasParent()) {
            navigation.addItem(
                    new GuiItem(ItemUtils.backItem(gui), event -> gui.showParent((Player) event.getWhoClicked())),
                    3, 0);
        }

        navigation.addItem(new GuiItem(ItemUtils.closeItem(), event -> new BukkitRunnable() {
            @Override
            public void run() {
                event.getWhoClicked().closeInventory();
            }
        }.runTaskLater(BukkitMain.getInstance(), 1)), closeX, 0);

        return navigation;
    }
}
