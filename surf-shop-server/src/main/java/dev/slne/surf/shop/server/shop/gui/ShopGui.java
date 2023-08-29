package dev.slne.surf.shop.server.shop.gui;

import java.util.ArrayList;
import java.util.List;

import dev.slne.surf.shop.server.shop.Shop;
import dev.slne.surf.shop.server.shop.gui.utils.GuiUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;

public abstract class ShopGui extends ChestGui {

    private Shop shop;
    private ShopGui parent;
    private Player viewingPlayer;

    /**
     * Creates a new shop gui.
     *
     * @param parent        the parent gui
     * @param rows          the amount of rows this gui should have
     * @param title         the title of this gui
     * @param shop          the shop
     * @param viewingPlayer the player viewing the shop
     */
    protected ShopGui(ShopGui parent, int rows, String title, Shop shop, Player viewingPlayer) {
        this(parent, rows, title, shop, viewingPlayer, true, true, true, true);
    }

    /**
     * Creates a new shop gui.
     *
     * @param parent            the parent gui
     * @param rows              the amount of rows this gui should have
     * @param title             the title of this gui
     * @param shop              the shop
     * @param viewingPlayer     the player viewing the shop
     * @param cancelTopClick    if the top inventory click should be cancelled
     * @param cancelTopDrag     if the top inventory drag should be cancelled
     * @param cancelBottomClick if the bottom inventory click should be cancelled
     * @param cancelBottomDrag  if the bottom inventory drag should be cancelled
     */
    @SuppressWarnings("java:S107")
    protected ShopGui(ShopGui parent, int rows, String title, Shop shop, Player viewingPlayer, boolean cancelTopClick,
            boolean cancelTopDrag, boolean cancelBottomClick, boolean cancelBottomDrag) {
        super(rows, title);

        if (rows < 2) {
            shop.unlock();
            throw new IllegalArgumentException("rows must be at least 2");
        }

        this.parent = parent;
        this.shop = shop;
        this.viewingPlayer = viewingPlayer;

        setOnTopClick(event -> event.setCancelled(cancelTopClick));
        setOnTopDrag(event -> event.setCancelled(cancelTopDrag));
        setOnBottomClick(event -> event.setCancelled(cancelBottomClick));
        setOnBottomDrag(event -> event.setCancelled(cancelBottomDrag));
        setOnOutsideClick(event -> event.setCancelled(true));

        setOnClose(event -> {
            if (!event.getReason().equals(Reason.OPEN_NEW) && !event.getReason().equals(Reason.UNKNOWN)) {
                shop.unlock();
            }
        });

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(rows - 1));
        addPane(GuiUtils.getNavigation(this, rows - 1));
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

    /**
     * @return the viewingPlayer
     */
    public Player getViewingPlayer() {
        return viewingPlayer;
    }

}
