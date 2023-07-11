package dev.slne.shop.shop.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;

import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.utils.GuiUtils;

public abstract class ShopGui extends ChestGui {

    private Shop shop;
    private ShopGui parent;

    /**
     * Creates a new shop gui.
     *
     * @param parent the parent gui
     * @param rows   the amount of rows this gui should have
     * @param title  the title of this gui
     * @param shop   the shop
     */
    protected ShopGui(ShopGui parent, int rows, String title, Shop shop) {
        super(rows, title);

        if (rows < 2) {
            shop.unlock();
            throw new IllegalArgumentException("rows must be at least 2");
        }

        this.parent = parent;
        this.shop = shop;

        setOnGlobalClick(event -> event.setCancelled(true));
        setOnGlobalDrag(event -> event.setCancelled(true));
        setOnClose(event -> {
            System.out.println(event.getReason());
            if (!event.getReason().equals(Reason.OPEN_NEW) && !event.getReason().equals(Reason.UNKNOWN)) {
                shop.unlock();
            }
        });

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(rows - 1));
        addPane(GuiUtils.getNavigation(this, rows - 1));

        update();
    }

    /**
     * Shows the parent gui to the player.
     *
     * @param player the player to show the gui to
     */
    public void showParent(Player player) {
        if (parent == null) {
            return;
        }

        parent.show(player);
        parent.update();
    }

    /**
     * @return the parent
     */
    public ShopGui getParent() {
        return parent;
    }

    /**
     * Returns if the gui has a parent.
     *
     * @return if the gui has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Walks through the parents of this gui and returns them.
     *
     * @return the parents of this gui
     */
    public List<ShopGui> walkParents() {
        List<ShopGui> parents = new ArrayList<>();

        ShopGui parentGui = getParent();
        while (parentGui != null) {
            parents.add(parentGui);
            parentGui = parentGui.getParent();
        }

        return parents;
    }

    /**
     * @return the shop
     */
    public Shop getShop() {
        return shop;
    }

}
