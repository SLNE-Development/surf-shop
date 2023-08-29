package dev.slne.surf.shop.server.shop.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfirmationGui extends ChestGui {

    private ChestGui previousGui;

    private Component questionLabel;
    private List<Component> questionLore;
    private Consumer<InventoryClickEvent> onConfirm;
    private Consumer<InventoryEvent> onCancel;

    /**
     * Creates a new confirmation gui
     *
     * @param previousGui the previous gui
     * @param onConfirm   the action when the user confirms
     * @param onCancel    the action when the user cancels
     */
    public ConfirmationGui(ChestGui previousGui, Consumer<InventoryClickEvent> onConfirm,
            Consumer<InventoryEvent> onCancel, Component questionLabel, List<Component> questionLore) {
        super(5, "Bestätigung erforderlich");

        this.previousGui = previousGui;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.questionLabel = questionLabel;
        this.questionLore = questionLore;

        setOnGlobalClick(event -> event.setCancelled(true));

        setOnClose(event -> cancel(event, (Player) event.getPlayer()));

        StaticPane confirmationPane = new StaticPane(0, 0, 9, 5);

        confirmationPane.addItem(new GuiItem(ItemUtils.confirmationConfirmItem(), this::confirm), 1, 2);

        confirmationPane.addItem(new GuiItem(ItemUtils.confirmationCancelItem(),
                event -> cancel(event, (Player) event.getWhoClicked())), 7, 2);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Es ist eine Bestätigung erforderlich...", NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.addAll(questionLore);
        lore.add(Component.empty());

        confirmationPane.addItem(
                new GuiItem(ItemUtils.confirmationQuestionItem(questionLabel,
                        lore.stream().toArray(size -> new Component[size]))),
                4, 2);

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(4));
        addPane(confirmationPane);
    }

    /**
     * Confirms the action
     *
     * @param event the event
     */
    public void confirm(InventoryClickEvent event) {
        if (onConfirm != null) {
            onConfirm.accept(event);
        }
    }

    /**
     * Cancels the action
     *
     * @param event  the event
     * @param player the player
     */
    public void cancel(InventoryEvent event, Player player) {
        if (onCancel != null) {
            onCancel.accept(event);
        }

        backToParent(player);
    }

    /**
     * Goes back to the previous gui
     *
     * @param player the player
     */
    public void backToParent(Player player) {
        if (previousGui != null) {
            previousGui.show(player);
            previousGui.update();
        } else {
            player.closeInventory();
        }
    }

    /**
     * @return the onCancel
     */
    public Consumer<InventoryEvent> getOnCancel() {
        return onCancel;
    }

    /**
     * @return the onConfirm
     */
    public Consumer<InventoryClickEvent> getOnConfirm() {
        return onConfirm;
    }

    /**
     * @return the previousGui
     */
    public Gui getPreviousGui() {
        return previousGui;
    }

    /**
     * @return the questionLabel
     */
    public Component getQuestionLabel() {
        return questionLabel;
    }

    /**
     * @return the questionLore
     */
    public List<Component> getQuestionLore() {
        return questionLore;
    }

    /**
     * @param onCancel the onCancel to set
     */
    public void setOnCancel(Consumer<InventoryEvent> onCancel) {
        this.onCancel = onCancel;
    }

    /**
     * @param onConfirm the onConfirm to set
     */
    public void setOnConfirm(Consumer<InventoryClickEvent> onConfirm) {
        this.onConfirm = onConfirm;
    }

}
