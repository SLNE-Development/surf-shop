package dev.slne.shop.util;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Permissions {

    /**
     * Allows the player to access the shop sell menu.
     */
    public static final String MENU_SELL = register("surf.shop.item.main-menu.sell");

    /**
     * TODO: Add description
     */
    public static final String MENU_OWNER = register("surf.shop.item.main-menu.owner");

    /**
     * Allows the player to access the shop edit menu.
     */
    public static final String MENU_EDIT = register("surf.shop.item.main-menu.edit");

    /**
     * Allows the player to see the shop info.
     */
    public static final String MENU_INFO = register("surf.shop.item.main-menu.info");

    /**
     * TODO: Add description
     */
    public static final String MENU_SHOP_ITEM = register("surf.shop.item.main-menu.shop-item");

    /**
     * Allows the player to buy items from the shop.
     */
    public static final String MENU_BUY = register("surf.shop.item.main-menu.buy");

    public static final String SELL_MENU_OWNER = register("surf.shop.item.sell-menu.owner");

    public static final String SELL_MENU_INFO = register("surf.shop.item.sell-menu.info");

    public static final String SELL_MENU_SHOP_ITEM = register("surf.shop.item.sell-menu.shop-item");

    /**
     * Parent permissions
     */
    private static final Permission allPermission = registerParent("surf.shop.*");


    /**
     * The {@link PluginManager} of the server.
     */
    private static final PluginManager pluginManager = Bukkit.getPluginManager();


    /**
     * Registers a new {@link Permission} with the given name and optionally parent permissions.
     *
     * @param permission The name of the permission to register.
     * @param parents    The optionally parent permissions of the permission.
     * @return The name of the registered permission.
     */
    @Contract("_, _ -> param1")
    private static String register(final @NotNull String permission, Permission @NotNull ... parents) {
        final Permission bukkitPermission = new Permission(permission);

        bukkitPermission.addParent(allPermission, true);

        for (Permission parent : parents) {
            bukkitPermission.addParent(parent, true);
        }

        pluginManager.addPermission(bukkitPermission);
        return permission;
    }

    /**
     * Registers a new parent permission with the given name and optional description.
     *
     * @param permission  The name of the permission to register.
     * @param description The optional description of the permission.
     * @return The registered parent permission.
     */
    private static @NotNull Permission registerParent(@NotNull String permission, @Nullable String description) {
        final Permission perm = new Permission(permission, description);
        pluginManager.addPermission(perm);
        return perm;
    }

    /**
     * Registers a new parent permission with the given name and no description.
     *
     * @param permission The name of the permission to register.
     * @return The registered parent permission.
     */
    private static @NotNull Permission registerParent(@NotNull String permission) {
        return registerParent(permission, null);
    }

    /**
     * Invokes the class.
     */
    public static void invoke() {
    }
}
