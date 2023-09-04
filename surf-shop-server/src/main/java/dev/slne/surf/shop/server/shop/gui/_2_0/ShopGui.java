package dev.slne.surf.shop.server.shop.gui._2_0;

import dev.slne.gui.api.chest.SurfChestGui;
import dev.slne.surf.shop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.*;

public abstract class ShopGui extends SurfChestGui implements SurfShopGui {

    private Shop shop;

    protected ShopGui(@NotNull Shop shop, @Nullable SurfShopGui parent, int rows, @NotNull Component title) {
        super(parent, rows, title);

        checkNotNull(shop, "Shop cannot be null");

        this.shop = shop;

        setOnClose(event -> {
            if (event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
                return;
            }

            shop.unlock();
        });
    }

    @Override
    public Shop getShop() {
        return shop;
    }

    @Override
    public void setShop(Shop shop) {
        checkNotNull(shop, "Shop cannot be null");
        this.shop = shop;
    }
}
