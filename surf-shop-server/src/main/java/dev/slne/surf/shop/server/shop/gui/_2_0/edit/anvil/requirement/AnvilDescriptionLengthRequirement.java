package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.util.ShopUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnvilDescriptionLengthRequirement implements AnvilRequirement {
    private final Shop shop;

    public AnvilDescriptionLengthRequirement(Shop shop) {
        this.shop = shop;
    }

    /**
     * Returns the description
     *
     * @param description  the description
     * @param stateColor   the state color
     * @param currentInput the current input
     * @return the description
     */
    @Override
    public List<Component> getDescription(List<Component> description, TextColor stateColor, String currentInput) {
        description.add(Component.text("Die Beschreibung darf maximal ", stateColor)
                .append(Component.text(Shop.MAX_DESCRIPTION_LENGTH, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" Zeichen lang sein.", stateColor)));

        return description;
    }

    /**
     * Returns if the input is met
     *
     * @param input the input
     * @return if the input is met
     */
    @Override
    public CompletableFuture<Boolean> isMet(String input) {
        return CompletableFuture.supplyAsync(() -> {
            final Component parsedDescription = shop.getMiniMessage().deserialize(input);
            final String plainDescription = ShopUtils.PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(parsedDescription);

            return plainDescription.length() <= Shop.MAX_DESCRIPTION_LENGTH;
        });
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
