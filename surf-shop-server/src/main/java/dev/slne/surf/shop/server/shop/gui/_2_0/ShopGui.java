package dev.slne.surf.shop.server.shop.gui._2_0;

import com.google.common.base.Preconditions;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.gui.api.SurfGui;
import dev.slne.surf.gui.api.chest.SurfChestGui;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ShopGui extends SurfChestGui {

    private final ServerShop shop;

    protected ShopGui(@NotNull ServerShop shop, @Nullable SurfGui parent, int rows, @NotNull Component title) {
        super(parent, rows, title);

        Preconditions.checkNotNull(shop, "ServerShop cannot be null");

        this.shop = shop;
    }

    /**
     * @return The shop that this gui is for
     */
    public ServerShop getShop() {
        return shop;
    }
}
