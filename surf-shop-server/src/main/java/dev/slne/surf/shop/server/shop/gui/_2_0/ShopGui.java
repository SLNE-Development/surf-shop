package dev.slne.surf.shop.server.shop.gui._2_0;

import com.google.common.base.Preconditions;
import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.chest.SurfChestGui;
import dev.slne.surf.shop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class ShopGui extends SurfChestGui {

    private final Shop shop;

    protected ShopGui(@NotNull Shop shop, @Nullable SurfGui parent, int rows, @NotNull Component title) {
        super(parent, rows, title);

        Preconditions.checkNotNull(shop, "Shop cannot be null");

        this.shop = shop;

        setOnClose(event -> {
            if (event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
                return;
            }

            shop.unlock();
        });
    }

    /**
     * @return The shop that this gui is for
     */
    public Shop getShop() {
        return shop;
    }


}
