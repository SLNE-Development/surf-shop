package dev.slne.surf.shop.server.shop.gui._2_0.edit;

import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShopEditMainMenu extends ShopGui {
    public ShopEditMainMenu(@NotNull Shop shop, @NotNull SurfGui parent, @NotNull Player viewingPlayer) {
        super(shop, parent, 4, Component.text("Shop - Bearbeiten", MessageManager.PRIMARY));

        final StaticPane pane = new StaticPane(0, 1);

        // sell / buy price ändern --> über anvil gui eingabe
        // amount ändern
        // Mitglieder (vielleicht direkt im main menu)
        // item ändern (auch mit shift + rechtsklick) --> item entfernen und gui schließen. Kann dann über shift + rechtsklick wieder gesetzt werden
        // lager bearbeiten --> neues gui
        // description line hinzufügen --> über anvil gui eingabe (kostet was?) Max. zwei zeilen mit color codes
        // limits --> neues gui wenn zu voll (global limits (einzelne item anzahl); player limits; spieler blockieren (das gleiche wie player limit auf 0 - brauchts das?); limit reset - vllt. auch automatisch einstellen lassem z.B. nach jedem tag resetten sich die alle limits


    }
}
