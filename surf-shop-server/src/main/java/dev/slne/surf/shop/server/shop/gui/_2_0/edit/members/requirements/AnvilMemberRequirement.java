package dev.slne.surf.shop.server.shop.gui._2_0.edit.members.requirements;

import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnvilMemberRequirement implements AnvilRequirement {

    private final Shop shop;
    private final boolean addMember;

    public AnvilMemberRequirement(@NotNull Shop shop, boolean addMember) {

        this.shop = shop;
        this.addMember = addMember;
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
        description.add(Component.text("Die Eingabe muss %s Mitglied des Shops sein.".formatted(addMember ? "kein" : "ein"), stateColor));

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
        return CompletableFuture.supplyAsync(() -> addMember != shop.isMember(Bukkit.getOfflinePlayer(input)));
    }
}
