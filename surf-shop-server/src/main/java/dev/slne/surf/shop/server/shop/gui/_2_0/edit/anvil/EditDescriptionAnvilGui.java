package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil;

import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.gui.api.anvil.requirement.requirements.AnvilLengthRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.util.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EditDescriptionAnvilGui extends SurfShopAnvilGui {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .useUnusualXRepeatedCharacterHexFormat()
            .hexCharacter('#')
            .build();

    private final Shop shop;

    public EditDescriptionAnvilGui(@Nullable SurfShopGui parent, Shop shop) {
        super(shop, parent, shop.description().map(SERIALIZER::serialize).orElse(" "), Component.text("Beschreibung bearbeiten", MessageManager.PRIMARY));
        this.shop = shop;
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new EditDescriptionAnvilGui(getParent(), shop);
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilLengthRequirement(0, ShopUtils.MAX_DESCRIPTION_LENGTH));

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
        shop.description(input).thenAcceptAsync(shop -> {
            player.sendMessage(MessageManager.changeDescription(shop));
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
