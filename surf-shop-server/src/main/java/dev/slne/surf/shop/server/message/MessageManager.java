package dev.slne.surf.shop.server.message;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.Colors;
import dev.slne.surf.shop.server.util.ShopUtils;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class MessageManager implements Colors {

    /**
     * Gets the prefix
     *
     * @return the prefix
     */
    public Component prefix() {
        return Component.text(">> ", DARK_SPACER)
                .append(Component.text("Shop", PRIMARY))
                .append(Component.text(" | ", DARK_SPACER));
    }

    /**
     * Returns a component which tells the user that they cannot place a shop next
     * to a chest
     *
     * @return the component
     */
    public static Component getCannotPlaceShopNextToChestComponent() {
        return prefix().append(Component.text("Du kannst keinen Shop neben einer Kiste platzieren.", ERROR));
    }

    /**
     * Returns a component which tells the user that they cannot place a shop next
     * to a shop
     *
     * @return the component
     */
    public static Component getCannotPlaceShopNextToShopComponent() {
        return prefix().append(Component.text("Du kannst keinen Shop neben einem Shop platzieren.", ERROR));
    }

    /**
     * Returns a component which tells the user that they cannot place a chest next
     * to a shop
     *
     * @return the component
     */
    public static Component getCannotPlaceChestNextToShopComponent() {
        return prefix().append(Component.text("Du kannst keine Kiste neben einem Shop platzieren.", ERROR));
    }

    /**
     * Returns a component which tells the user that their shop was created
     *
     * @return the component
     */
    public static Component getShopCreatedSuccessfullyComponent() {
        return prefix().append(Component.text("Dein Shop wurde erfolgreich erstellt.", SUCCESS));
    }

    /**
     * Returns a component which tells the user that their shop was removed
     * successfully
     *
     * @return the component
     */
    public static Component getShopRemovedSuccessfullyComponent() {
        return prefix().append(Component.text("Dein Shop wurde erfolgreich entfernt.", SUCCESS));
    }

    /**
     * Returns a component which tells the user that their shop was created
     * unsuccessfully
     *
     * @return the component
     */
    public static Component getShopCreatedFailureComponent() {
        return prefix().append(Component.text("Dein Shop konnte nicht erstellt werden.", ERROR));
    }

    /**
     * Returns a component which tells the user that their shop was removed
     * unsuccessfully
     *
     * @return the component
     */
    public static Component getShopRemovedFailureComponent() {
        return prefix().append(Component.text("Dein Shop konnte nicht entfernt werden.", ERROR));
    }

    /**
     * Returns a component which tells the user that they do not own the shop
     *
     * @return the component
     */
    public static Component getPlayerNotOwningShopComponent() {
        return prefix().append(Component.text("Dieser Shop gehört dir nicht.", ERROR));
    }

    /**
     * Returns a component which tells the user that the shop is not empty
     *
     * @return the component
     */
    public static Component getShopNotEmptiedComponent() {
        return prefix().append(Component.text("Das Inventar des Shops ist nicht leer.", ERROR));
    }

    /**
     * Returns a component which tells the user that the owner or a member has
     * requested the shop gui
     *
     * @param isOwner  whether the player is the owner
     * @param isMember whether the player is a member
     * @param closer   the player who closed the shop
     * @return the component
     */
    public static Component getClosedDueToEditorRequest(boolean isOwner, boolean isMember, Player closer) {
        Component message;

        if (isOwner) {
            message = Component.text("Der Shop wurde durch den Besitzer ", INFO)
                    .append(ShopUtils.getDisplayName(closer))
                    .append(Component.text(" geschlossen.", INFO));
        } else if (isMember) {
            message = Component.text("Der Shop wurde durch ein Mitglied ", INFO)
                    .append(ShopUtils.getDisplayName(closer))
                    .append(Component.text(" geschlossen.", INFO));
        } else {
            message = Component.text("Der Shop wurde durch ", INFO)
                    .append(ShopUtils.getDisplayName(closer))
                    .append(Component.text(" geschlossen.", INFO));
        }

        return prefix().append(message);
    }

    /**
     * Returns a component which tells the user that the shop is locked
     *
     * @param lockedBy the player who locked the shop
     * @return the component
     */
    public static @NotNull Component getShopIsLockedComponent(@Nullable Player lockedBy) {
        if (lockedBy != null) {
            return prefix().append(Component.text("Der Shop wird aktuell durch ", INFO))
                    .append(ShopUtils.getDisplayName(lockedBy))
                    .append(Component.text(" verwendet.", INFO));
        }

        return prefix().append(Component.text("Der Shop wird aktuell durch einen anderen Benutzer verwendet.", INFO));
    }

    /**
     * Returns a component which tells the user that the shop is not setup
     *
     * @return the component
     */
    public static Component getShopIsNotSetupComponent() {
        return prefix().append(Component.text("Der Shop wurde noch nicht eingerichtet.", ERROR));
    }

    /**
     * Returns a component which tells the user that they bought x items
     *
     * @param boughtItemstack the itemstack bought
     * @param amount          the amount bought
     * @return the component
     */
    public static Component getShopBoughtAmountBuyerComponent(ItemStack boughtItemstack, int amount) {
        TextComponent.Builder builder = Component.text();

        builder.append(prefix());
        builder.append(Component.text("Du hast ", INFO));
        builder.append(Component.text(amount, VARIABLE_VALUE));
        builder.append(Component.text("x ", INFO));

        builder.append(getItemStackComponent(boughtItemstack));
        builder.append(Component.text(" gekauft.", INFO));

        return builder.build();
    }

    /**
     * Returns a component which tells the user that they sold x items
     *
     * @param buyer           the player who bought the item
     * @param boughtItemStack the itemstack bought
     * @param amount          the amount bought
     * @return the component
     */
    public static @NotNull Component getShopBoughtAmountOwnerComponent(@Nullable Player buyer, ItemStack boughtItemStack, int amount) {
        TextComponent.Builder builder = Component.text();

        builder.append(prefix());

        if (buyer != null) {
            builder.append(ShopUtils.getDisplayName(buyer));
        } else {
            builder.append(Component.text("Jemand", VARIABLE_VALUE));
        }

        builder.append(Component.text(" hat ", INFO));
        builder.append(Component.text(amount, VARIABLE_VALUE));
        builder.append(Component.text("x ", INFO));

        builder.append(getItemStackComponent(boughtItemStack));
        builder.append(Component.text(" gekauft.", INFO));

        return builder.build();
    }

    /**
     * Returns an itemstack component
     *
     * @param itemStack the itemstack
     * @return the component
     */
    public static @NotNull Component getItemStackComponent(@NotNull ItemStack itemStack) {
        Component name = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
                ? itemStack.getItemMeta().displayName()
                : Component.text(itemStack.getType().name());
        assert name != null;
        name = name.colorIfAbsent(VARIABLE_VALUE);

        List<Component> lore = itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()
                ? itemStack.getItemMeta().lore()
                : new ArrayList<>();

        TextComponent.Builder itemStackBuilder = Component.text();
        itemStackBuilder.append(name);

        assert lore != null;
        for (Component loreComponent : lore) {
            itemStackBuilder.append(Component.newline());
            itemStackBuilder.append(loreComponent);
        }

//        return name.hoverEvent(HoverEvent.showText(itemStackBuilder.build()));
        return itemStack.displayName().colorIfAbsent(VARIABLE_VALUE); // TODO: 04.11.2023 is this enough?
    }

    /**
     * Returns a component which tells the user that they cannot accept those items
     *
     * @param amount the amount tried to add
     * @return the component
     */
    public static Component getInventoryCannotAcceptNItemsComponent(int amount) {
        return prefix().append(Component.text("Dein Inventar kann die Menge von ", ERROR))
                .append(Component.text(amount, VARIABLE_VALUE)).append(Component.text(" Items", VARIABLE_VALUE))
                .append(Component.text(" nicht aufnehmen.", ERROR));
    }

    public static Component getShopAlreadyRemovingComponent() {
        return prefix().append(Component.text("Der Shop wird bereits entfernt.", ERROR));
    }

    public static Component getShopNotSellingComponent() {
        return prefix().append(Component.text("Der Shop verkauft aktuell nichts.", ERROR));
    }

    public static Component getShopNotBuyingComponent() {
        return prefix().append(Component.text("Der Shop kauft aktuell nichts an.", ERROR));
    }

    public static Component getNotEnoughMoneyComponent() {
        return prefix().append(Component.text("Du besitzt nicht genügend Geld.", ERROR));
    }

    public static Component getTransactionErrorComponent() {
        return prefix().append(Component.text("Es ist ein Fehler bei der Transaktion aufgetreten.", ERROR));
    }

    public static Component getErrorComponent() {
        return prefix().append(Component.text("Es ist ein Fehler aufgetreten.", ERROR));
    }

    public static Component changeSellPrice(Shop shop) {
        if (shop.isSelling()) {
            return prefix()
                    .append(Component.text("Du hast den Verkaufspreis auf ", SUCCESS))
                    .append(shop.renderSellPrice())
                    .append(Component.text(" gesetzt.", SUCCESS));
        } else {
            return prefix()
                    .append(Component.text("Du hast den Verkaufspreis ", SUCCESS))
                    .append(Component.text("deaktiviert", VARIABLE_VALUE))
                    .append(Component.text(".", SUCCESS));
        }
    }

    public static Component changeBuyPrice(Shop shop) {
        if (shop.isBuying()) {
            return prefix()
                    .append(Component.text("Du hast den Ankaufspreis auf ", SUCCESS))
                    .append(shop.renderBuyPrice())
                    .append(Component.text(" gesetzt.", SUCCESS));
        } else {
            return prefix()
                    .append(Component.text("Du hast den Ankaufspreis ", SUCCESS))
                    .append(Component.text("deaktiviert", VARIABLE_VALUE))
                    .append(Component.text(".", SUCCESS));
        }
    }

    public static Component changeQuantity(Shop shop) {
        return prefix()
                .append(Component.text("Die Stückzahl wurde auf ", MessageManager.SUCCESS))
                .append(Component.text(shop.quantity(), MessageManager.VARIABLE_VALUE))
                .append(Component.text(" gesetzt.", MessageManager.SUCCESS));
    }

    public static Component changeDescription(Shop shop) {
        return shop.description().map(component -> prefix() // If the description is present
                        .append(Component.text("Die Beschreibung wurde auf ", MessageManager.SUCCESS))
                        .append(Component.text("[", MessageManager.VARIABLE_VALUE))
                        .append(component)
                        .append(Component.text("]", MessageManager.VARIABLE_VALUE))
                        .append(Component.text(" gesetzt.", MessageManager.SUCCESS)))

                .orElseGet(() -> prefix() // If the description is not present
                        .append(Component.text("Die Beschreibung wurde ", MessageManager.SUCCESS))
                        .append(Component.text("deaktiviert.", NamedTextColor.RED)));
    }

    public static Component addMember(OfflinePlayer player) {
        return prefix()
                .append(Component.text("Du hast ", MessageManager.SUCCESS))
                .append(Component.text(Objects.requireNonNull(player.getName()), MessageManager.VARIABLE_VALUE))
                .append(Component.text(" als Mitglied hinzugefügt.", MessageManager.SUCCESS));
    }

    public static Component removeMember(OfflinePlayer player) {
        return prefix()
                .append(Component.text("Du hast ", MessageManager.SUCCESS))
                .append(Component.text(Objects.requireNonNull(player.getName()), MessageManager.VARIABLE_VALUE))
                .append(Component.text(" als Mitglied entfernt.", MessageManager.SUCCESS));
    }

    public static Component addItems(Shop shop, int addAmount) {
        return prefix()
                .append(Component.text("Du hast ", MessageManager.SUCCESS))
                .append(Component.text(addAmount, MessageManager.VARIABLE_VALUE))
                .append(Component.text("x ", MessageManager.SUCCESS))
                .append(getItemStackComponent(Objects.requireNonNull(shop.item().orElse(null))))
                .append(Component.text(" hinzugefügt.", MessageManager.SUCCESS));
    }

    public static Component removeItems(Shop shop, int removeAmount) {
        return prefix()
                .append(Component.text("Du hast ", MessageManager.SUCCESS))
                .append(Component.text(removeAmount, MessageManager.VARIABLE_VALUE))
                .append(Component.text("x ", MessageManager.SUCCESS))
                .append(getItemStackComponent(Objects.requireNonNull(shop.item().orElse(null))))
                .append(Component.text(" entfernt.", MessageManager.SUCCESS));
    }

    public static Component getShopBuyLimitComponent(Shop shop) {
        return prefix()
                .append(Component.text("Du kannst maximal ", MessageManager.ERROR))
                .append(Component.text(shop.buyLimit(), MessageManager.VARIABLE_VALUE))
                .append(Component.text("x ", MessageManager.ERROR))
                .append(shop.renderItem())
                .append(Component.text(" verkaufen.", MessageManager.ERROR));
    }

    /**
     * Returns a component which tells the user that they could not add the items
     *
     * @param shop   the shop
     * @param amount the amount
     * @return the component
     */
    public static Component addItemsFailed(Shop shop, int amount) {
        return prefix()
                .append(Component.text("Du konntest ", MessageManager.ERROR))
                .append(Component.text(amount, MessageManager.VARIABLE_VALUE))
                .append(Component.text("x ", MessageManager.ERROR))
                .append(getItemStackComponent(Objects.requireNonNull(shop.item().orElse(null))))
                .append(Component.text(" nicht hinzufügen.", MessageManager.ERROR));
    }
}
