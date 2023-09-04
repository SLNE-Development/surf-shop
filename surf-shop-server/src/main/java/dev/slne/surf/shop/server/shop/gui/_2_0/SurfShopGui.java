package dev.slne.surf.shop.server.shop.gui._2_0;

import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import dev.slne.gui.api.SurfGui;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.BukkitMain;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface SurfShopGui extends SurfGui {

    default void backToParent(HumanEntity viewer, Shop shop) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasParent()) {
                    final NamedGui gui = Objects.requireNonNull(getParent()).getGui();

                    setShop(shop);
                    gui.show(viewer);
                    gui.update();

                    return;
                }

                viewer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }.runTask(BukkitMain.getInstance());
    }

    /**
     * Returns the gui
     *
     * @return the gui
     */
    @Override
    @NotNull NamedGui getGui();

    Shop getShop();

    void setShop(Shop shop);
}
