package dev.slne.shop.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MessageManager {

    public static final TextColor PRIMARY = TextColor.fromHexString("#3b92d1");
    public static final TextColor SECONDARY = TextColor.fromHexString("#5b5b5b");

    public static final TextColor INFO = TextColor.fromHexString("#40d1db");
    public static final TextColor SUCCESS = TextColor.fromHexString("#65ff64");
    public static final TextColor WARNING = TextColor.fromHexString("#f9c353");
    public static final TextColor ERROR = TextColor.fromHexString("#ee3d51");

    public static final TextColor VARIABLE_KEY = MessageManager.INFO;
    public static final TextColor VARIABLE_VALUE = MessageManager.WARNING;
    public static final TextColor SPACER = NamedTextColor.GRAY;
    public static final TextColor DARK_SPACER = NamedTextColor.DARK_GRAY;

    /**
     * Utility class
     */
    private MessageManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the prefix
     *
     * @return the prefix
     */
    public static Component prefix() {
        return Component.text(">> ", NamedTextColor.DARK_GRAY).append(Component.text("Shop", PRIMARY))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY));
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
        return prefix().append(Component.text("Dieser Shop gehört nicht dir.", ERROR));
    }

    /**
     * Returns a component which tells the user that the shop is not empty
     *
     * @return the component
     */
    public static Component getShopNotEmptiedComponent() {
        return prefix().append(Component.text("Das Inventar des Shops ist nicht leer.", ERROR));
    }
}
