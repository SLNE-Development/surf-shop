package dev.slne.surf.shop.server.shop.gui._2_0.edit.members.actions;

import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.player.AnvilOfflinePlayerRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.SurfShopAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.members.requirements.AnvilMemberRequirement;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopEditMembersRemoveAnvilMenu extends SurfShopAnvilGui {

    private final Shop shop;

    public ShopEditMembersRemoveAnvilMenu(@Nullable SurfShopGui parent, @NotNull Shop shop) {
        super(shop, parent, Component.text("Mitglied entfernen", MessageManager.PRIMARY));
        this.shop = shop;
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new ShopEditMembersRemoveAnvilMenu(getParent(), shop);
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilOfflinePlayerRequirement());
        requirements.add(new AnvilMemberRequirement(shop, false));

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
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input.trim().replace(" ", ""));

        assert shop.isMember(offlinePlayer) : "Should never happen as the requirement should prevent this";

        shop.removeMember(offlinePlayer).thenAcceptAsync(updatedShop -> {
            player.sendMessage(MessageManager.removeMember(offlinePlayer));
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
