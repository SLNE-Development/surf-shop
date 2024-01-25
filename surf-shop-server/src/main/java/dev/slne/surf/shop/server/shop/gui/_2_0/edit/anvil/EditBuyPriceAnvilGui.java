package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil;

import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement.AnvilOnlyNumberRequirement;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EditBuyPriceAnvilGui extends SurfShopAnvilGui {
    private final Shop shop;

    /**
     * Creates a new anvil gui.
     *
     * @param parent the parent gui
     */
    public EditBuyPriceAnvilGui(@Nullable SurfShopGui parent, Shop shop) {
        super(shop, parent, String.valueOf(shop.buyPrice()), Component.text("Ankaufspreis bearbeiten", MessageManager.PRIMARY));
        this.shop = shop;
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new EditBuyPriceAnvilGui(getParent(), shop);
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilOnlyNumberRequirement<>(0, Shop.MAX_BUY_PRICE));

        return requirements;
    }

    /**
     * Called when the player submits the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public List<AnvilGUI.ResponseAction> onSubmit(Player player, String input) {
        double buyPrice = Double.parseDouble(input);

        shop.buyPrice(buyPrice).thenAcceptAsync(updatedShop -> {
            player.sendMessage(MessageManager.changeBuyPrice(updatedShop));
            backToParent(player, updatedShop);
        });

        return new ArrayList<>();
    }

    /**
     * Called when the player cancels the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public List<AnvilGUI.ResponseAction> onCancel(Player player, String input) {
        backToParent(player);

        return new ArrayList<>();
    }
}
