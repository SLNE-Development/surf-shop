package dev.slne.surf.shop.server.shop.gui._2_0.confirmation;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.chest.SurfChestGui;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConfirmationGui extends SurfChestGui { // TODO: Does this work with the locked shop?

    @Getter
    private final Component questionLabel;
    @Getter
    private final List<Component> questionLore;
    @Getter
    @Setter
    private @Nullable Consumer<InventoryClickEvent> onConfirm;
    @Getter
    @Setter
    private @Nullable Consumer<InventoryEvent> onCancel;

    public ConfirmationGui(
            @NotNull SurfGui parent,
            @Nullable Consumer<InventoryClickEvent> onConfirm,
            @Nullable Consumer<InventoryEvent> onCancel,
            @NotNull Component questionLabel,
            @NotNull Component @NotNull ... questionLore
    ) {
        super(parent, 5, Component.text("Bestätigung erforderlich", MessageManager.ERROR));

        this.questionLabel = questionLabel;
        this.questionLore = Arrays.asList(questionLore);
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        final StaticPane confirmationPane = new StaticPane(0, 0, 9, 5);

        confirmationPane.addItem(new GuiItem(ItemUtils.confirmationConfirmItem(), this::confirm), 1, 2);

        confirmationPane.addItem(new GuiItem(ItemUtils.confirmationCancelItem(),
                event -> cancel(event, event.getWhoClicked())), 7, 2);

        final List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Es ist eine Bestätigung erforderlich...", NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.addAll(Arrays.stream(questionLore).toList());
        lore.add(Component.empty());

        confirmationPane.addItem(
                new GuiItem(ItemUtils.confirmationQuestionItem(questionLabel,
                        lore.toArray(Component[]::new))),
                4, 2);

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
    public void cancel(InventoryEvent event, HumanEntity player) {
        if (onCancel != null) {
            onCancel.accept(event);
        }

        backToParent(player);
    }
}
