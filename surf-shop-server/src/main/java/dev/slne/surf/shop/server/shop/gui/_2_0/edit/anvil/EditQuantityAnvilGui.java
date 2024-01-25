package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement.AnvilQuantityRequirement;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class EditQuantityAnvilGui extends SurfShopAnvilGui {

    /**
     * Creates a new quantity anvil gui
     *
     * @param parent the parent gui
     * @param shop   the shop to edit the quantity of
     */
    public EditQuantityAnvilGui(@Nullable SurfShopGui parent, Shop shop) {
        super(shop, parent, String.valueOf(shop.quantity()), Component.text("Stückzahl bearbeiten", MessageManager.PRIMARY));
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfShopAnvilGui copyGui() {
        return new EditQuantityAnvilGui(getParent(), getShop());
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilQuantityRequirement(getShop().item().map(ItemStack::getMaxStackSize).orElse(1)));
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
        getShop().quantity(NumberUtils.parseNumber(input, int.class)).thenAcceptAsync(shop -> {
            player.sendMessage(MessageManager.changeQuantity(shop));
            backToParent(player, shop);
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
