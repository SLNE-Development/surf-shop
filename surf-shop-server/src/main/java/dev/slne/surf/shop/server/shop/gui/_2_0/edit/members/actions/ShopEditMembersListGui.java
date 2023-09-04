package dev.slne.surf.shop.server.shop.gui._2_0.edit.members.actions;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.gui.api.SurfGui;
import dev.slne.gui.api.utils.pagination.PageController;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.util.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopEditMembersListGui extends ShopGui {

    private final StaticPane navigationPane;
    private final PaginatedPane paginatedPane;

    public ShopEditMembersListGui(@NotNull Shop shop, @Nullable SurfShopGui parent) {
        super(shop, parent, 5, Component.text("Mitglieder", MessageManager.PRIMARY));

        paginatedPane = new PaginatedPane(0, 1, 9, 3);
        navigationPane = new StaticPane(0, 4, 9, 1);

        addPane(paginatedPane);
        addPane(navigationPane);

        update();
    }

    /**
     * Update the gui for everyone
     */
    @Override
    public void update() {
        final List<GuiItem> memberItems = new ArrayList<>();

        for (ShopMember member : getShop().getMembers()) {
            final OfflinePlayer memberPlayer = member.getPlayer();
            final GuiItem removeMemberItem = new GuiItem(
                    ItemUtils.head(
                            memberPlayer,
                            Component.text(Objects.requireNonNull(memberPlayer.getName()), MessageManager.PRIMARY),
                            Component.empty(),
                            Component.text("Shift + Links-Klick", MessageManager.VARIABLE_VALUE)
                                    .append(Component.text(" um das Mitglied zu entfernen.", MessageManager.SECONDARY))
                    ),
                    event -> {
                        if (event.getClick() != ClickType.SHIFT_LEFT) {
                            return;
                        }

                        getShop().removeMember(memberPlayer).thenAcceptAsync(updatedShop ->
                                event.getWhoClicked().sendMessage(MessageManager.removeMember(memberPlayer)));
                        update();
                    }
            );

            memberItems.add(removeMemberItem);
        }

        paginatedPane.populateWithGuiItems(memberItems);
        updateNavigation();
        super.update();
    }

    /**
     * Updates the navigation pane
     */
    private void updateNavigation() {
        navigationPane.addItem(
                PageController.PREVIOUS.toGuiItem(
                        this,
                        Component.text("Zurück", MessageManager.PRIMARY),
                        paginatedPane,
                        dev.slne.gui.api.utils.ItemUtils.paneItem()
                ),
                0,
                0
        );

        navigationPane.addItem(
                PageController.NEXT.toGuiItem(
                        this,
                        Component.text("Weiter",
                                MessageManager.PRIMARY
                        ),
                        paginatedPane,
                        dev.slne.gui.api.utils.ItemUtils.paneItem()
                ),
                8,
                0
        );
    }
}
