package dev.slne.surf.shop.server.shop.gui._2_0.edit.storage.actions;

import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.SurfShopAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement.AnvilOnlyNumberRequirement;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShopEditStorageAddAnvilMenu extends SurfShopAnvilGui {

    private final Shop shop;
    private final HumanEntity clickedHuman;

    /**
     * Creates a new anvil gui.
     *
     * @param parent       the parent gui
     * @param shop         the shop
     * @param clickedHuman the clicked human
     */
    public ShopEditStorageAddAnvilMenu(@Nullable SurfShopGui parent, @NotNull Shop shop,
                                       @NotNull HumanEntity clickedHuman) {
        super(shop, parent, String.valueOf(similarItems(shop, clickedHuman)),
                Component.text("Items hinzufügen", MessageManager.PRIMARY));
        this.shop = shop;
        this.clickedHuman = clickedHuman;
    }

    private static int similarItems(Shop shop, @NotNull HumanEntity clickedHuman) {
        return Arrays.stream(clickedHuman.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.isSimilar(shop.item()))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new ShopEditStorageAddAnvilMenu(getParent(), shop, clickedHuman);
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     *
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(@NotNull List<AnvilRequirement> requirements) {
        requirements.add(new AnvilOnlyNumberRequirement<>(0, similarItems(shop, clickedHuman)));
        return requirements;
    }

    /**
     * Called when the player submits the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public List<AnvilGUI.ResponseAction> onSubmit(Player player, @NotNull String input) {
        final int amount = Integer.parseInt(input.trim().replace(" ", ""));

        shop.increaseAmount(player.getUniqueId(), amount).thenAcceptAsync(result -> {
            if (result == ShopTransactionResult.SUCCESS) {
                player.sendMessage(MessageManager.addItems(shop, amount));
                backToParent(player, shop);
                return;
            }

            player.sendMessage(MessageManager.addItemsFailed(shop, amount));
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
