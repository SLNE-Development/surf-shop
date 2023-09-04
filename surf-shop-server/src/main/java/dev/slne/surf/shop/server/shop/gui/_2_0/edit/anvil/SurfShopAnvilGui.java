package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil;

import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SurfShopAnvilGui extends SurfAnvilGui implements SurfShopGui {
    private Shop shop;
    private final SurfShopGui parent;

    /**
     * Creates a new anvil gui.
     *
     * @param parent       the parent gui
     * @param defaultInput the default input
     * @param title        the title of this gui
     * @param pluginPrefix the prefix of the plugin
     */
    public SurfShopAnvilGui(@NotNull Shop shop, @Nullable SurfShopGui parent, @NotNull String defaultInput, @NotNull Component title, @Nullable Component pluginPrefix) {
        super(parent, defaultInput, title, pluginPrefix);
        this.shop = shop;
        this.parent = parent;
    }

    /**
     * Creates a new anvil gui.
     *
     * @param parent       the parent gui
     * @param title        the title of this gui
     * @param pluginPrefix the prefix of the plugin
     */
    public SurfShopAnvilGui(@NotNull Shop shop, @Nullable SurfShopGui parent, @NotNull Component title, @Nullable Component pluginPrefix) {
        super(parent, title, pluginPrefix);
        this.shop = shop;
        this.parent = parent;
    }

    /**
     * Creates a new anvil gui.
     *
     * @param parent the parent gui
     * @param title  the title of this gui
     */
    public SurfShopAnvilGui(@NotNull Shop shop, @Nullable SurfShopGui parent, @NotNull Component title) {
        super(parent, title);
        this.shop = shop;
        this.parent = parent;
    }

    /**
     * Creates a new anvil gui.
     *
     * @param parent       the parent gui
     * @param defaultInput the default input
     * @param title        the title of this gui
     */
    public SurfShopAnvilGui(@NotNull Shop shop, @Nullable SurfShopGui parent, @NotNull String defaultInput, @NotNull Component title) {
        super(parent, defaultInput, title);
        this.shop = shop;
        this.parent = parent;
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public abstract SurfAnvilGui copyGui();

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public abstract List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements);

    /**
     * Called when the player submits the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public abstract List<AnvilGUI.ResponseAction> onSubmit(Player player, String input);

    /**
     * Called when the player cancels the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public abstract List<AnvilGUI.ResponseAction> onCancel(Player player, String input);

    public Shop getShop() {
        return shop;
    }

    @Override
    public void setShop(Shop shop) {
        this.shop = shop;
    }

    @Override
    public @Nullable SurfShopGui getParent() {
        return parent;
    }
}
