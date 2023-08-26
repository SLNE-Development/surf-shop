package dev.slne.shop.shop.gui._2_0.edit;

import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui._2_0.ShopGui;
import dev.slne.surf.gui.api.SurfGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopEditMainMenu extends ShopGui {
    public ShopEditMainMenu(@NotNull Shop shop, @NotNull SurfGui parent, @NotNull Player viewingPlayer) {
        super(shop, parent, 4, Component.text("Shop - Bearbeiten", MessageManager.PRIMARY));
    }
}
