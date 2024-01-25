package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil;

import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement.AnvilSellRequirement;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class EditSellPriceAnvilGui extends SurfShopAnvilGui {

    private final Shop shop;

    /**
     * Creates a new anvil gui.
     *
     * @param parent the parent gui
     */
    public EditSellPriceAnvilGui(@Nullable SurfShopGui parent, Shop shop) {
        super(shop, parent, String.valueOf(shop.sellPrice()), Component.text("Verkaufspreis bearbeiten", MessageManager.PRIMARY));
        this.shop = shop;
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new EditSellPriceAnvilGui(getParent(), shop);
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilSellRequirement(Shop.MAX_SELL_PRICE));

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
        double sellPrice = NumberUtils.parseNumber(input, double.class);

        shop.sellPrice(sellPrice).thenAcceptAsync(updatedShop -> {
            player.sendMessage(MessageManager.changeSellPrice(shop)); // TODO: change to updatedShop
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
