package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.server.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnvilSellRequirement implements AnvilRequirement {

    private final double maximumSellPrice;

    @Contract(pure = true)
    public AnvilSellRequirement(double maximumSellPrice) {
        this.maximumSellPrice = maximumSellPrice;
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
        description.add(Component.text("Die Eingabe darf nur aus gültigen Gleitkommazahlen bestehen.", stateColor));
        description.add(Component.text("Die Eingabe darf nicht größer als ", stateColor)
                .append(Component.text(maximumSellPrice, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" und nicht kleiner als ", stateColor))
                .append(Component.text(0, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" sein.", stateColor)));

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
        if (!StringUtils.hasText(input)) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            final double parsed = NumberUtils.parseNumber(input, double.class);

            return CompletableFuture.completedFuture(parsed <= maximumSellPrice && parsed > 0);
        } catch (NumberFormatException exception) {
            return CompletableFuture.completedFuture(false);
        }
    }
}
