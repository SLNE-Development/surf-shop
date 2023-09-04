package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnvilOnlyNumberRequirement<T extends Number> implements AnvilRequirement {

    private final T min;
    private final T max;

    public AnvilOnlyNumberRequirement(T min, T max) {
        this.min = min;
        this.max = max;
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
        description.add(Component.text("Die Eingabe darf nur aus %s-Zahlen bestehen.".formatted(min.getClass().getSimpleName()), stateColor));
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

        try {
            double parsed = Double.parseDouble(input);

            if (parsed < min.doubleValue() || parsed > max.doubleValue()) {
                return CompletableFuture.completedFuture(false);
            }
        } catch (NumberFormatException exception) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }
}
