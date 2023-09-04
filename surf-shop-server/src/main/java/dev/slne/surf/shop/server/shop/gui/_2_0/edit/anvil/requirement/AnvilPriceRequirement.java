package dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.server.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnvilPriceRequirement implements AnvilRequirement {

    private final double min;
    private final double max;

    public AnvilPriceRequirement(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public List<Component> getDescription(List<Component> list, TextColor textColor, String s) {
        list.add(Component.text("Die Eingabe darf nur aus gültigen Gleitkommazahlen bestehen.", textColor));
        list.add(Component.text("Die Eingabe muss zwischen ", textColor)
                .append(Component.text(min, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" und ", textColor))
                .append(Component.text(max, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" liegen.", textColor)));

        return list;
    }

    @Override
    public CompletableFuture<Boolean> isMet(String s) {
        if (s.isEmpty() || s.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            double parsed = Double.parseDouble(s.trim().replace(",", ".").replace(" ", ""));

            if (parsed < min || parsed > max) {
                return CompletableFuture.completedFuture(false);
            }
        } catch (NumberFormatException exception) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }
}
