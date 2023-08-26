package dev.slne.shop.shop.gui._2_0;

import com.google.common.base.Preconditions;
import dev.slne.shop.shop.Shop;
import dev.slne.surf.gui.api.SurfGui;
import dev.slne.surf.gui.api.chest.SurfChestGui;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ShopGui extends SurfChestGui {

    private final Shop shop;

    protected ShopGui(@NotNull Shop shop, @Nullable SurfGui parent, int rows, @NotNull Component title) {
        super(parent, rows, title);

        Preconditions.checkNotNull(shop, "Shop cannot be null");

        this.shop = shop;
    }

    /**
     * @return The shop that this gui is for
     */
    public Shop getShop() {
        return shop;
    }
}
