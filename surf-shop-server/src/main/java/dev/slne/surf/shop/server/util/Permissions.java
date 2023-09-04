package dev.slne.surf.shop.server.util;

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
    public static final String MENU_SELL;

    /**
     * TODO: Add description
     */
    public static final String MENU_OWNER;

    /**
     * Allows the player to access the shop edit menu.
     */
    public static final String MENU_EDIT;

    /**
     * Allows the player to see the shop info.
     */
    public static final String MENU_INFO;

    /**
     * TODO: Add description
     */
    public static final String MENU_SHOP_ITEM;

    /**
     * Allows the player to sell items from the shop.
     */
    public static final String MENU_BUY;

    public static final String SELL_MENU_OWNER;

    public static final String SELL_MENU_INFO;

    public static final String SELL_MENU_SHOP_ITEM;

    public static final String SELL_MENU_BUY;

    public static final String RESET_AMOUNT;

    public static final String DECREASE_1000;

    public static final String DECREASE_100;

    public static final String DECREASE_10;

    public static final String DECREASE_1;

    public static final String INCREASE_1;

    public static final String INCREASE_10;

    public static final String INCREASE_100;

    public static final String INCREASE_1000;

    public static final String SHOP_EDIT_SELL_PRICE;

    public static final String SHOP_EDIT_BUY_PRICE;

    public static final String SHOP_EDIT_AMOUNT;

    public static final String SHOP_EDIT_MEMBERS;

    public static final String SHOP_EDIT_STORAGE;

    public static final String SHOP_EDIT_DESCRIPTION;

    public static final String SHOP_EDIT_GLOBAL_LIMITS;

    public static final String SHOP_EDIT_PLAYER_LIMITS;

    public static final String SHOP_EDIT_BLOCK_PLAYER;

    public static final String SHOP_MENU_EDIT_MEMBERS_ADD;

    public static final String SHOP_MENU_EDIT_MEMBERS_REMOVE;

    public static final String SHOP_MENU_EDIT_MEMBERS_LIST;

    public static final String SHOP_MENU_EDIT_STORAGE_ADD;

    public static final String SHOP_MENU_EDIT_STORAGE_REMOVE;

    public static final String SHOP_MENU_EDIT_STORAGE_LIST;

    /**
     * Parent permissions
     */
    private static final Permission allPermission;


    /**
     * The {@link PluginManager} of the server.
     */
    private static final PluginManager pluginManager;


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

    static {
        pluginManager = Bukkit.getPluginManager();
        allPermission = registerParent("surf.shop.*");

        MENU_SELL = register("surf.shop.item.main-menu.sell");
        MENU_OWNER = register("surf.shop.item.main-menu.owner");
        MENU_EDIT = register("surf.shop.item.main-menu.edit");
        MENU_INFO = register("surf.shop.item.main-menu.info");
        MENU_SHOP_ITEM = register("surf.shop.item.main-menu.shop-item");
        MENU_BUY = register("surf.shop.item.main-menu.buy");
        SELL_MENU_OWNER = register("surf.shop.item.sell-menu.owner");
        SELL_MENU_INFO = register("surf.shop.item.sell-menu.info");
        SELL_MENU_SHOP_ITEM = register("surf.shop.item.sell-menu.shop-item");
        SELL_MENU_BUY = register("surf.shop.item.sell-menu.buy");
        RESET_AMOUNT = register("surf.shop.item.sell-menu.reset");
        DECREASE_1000 = register("surf.shop.item.sell-menu.decrease-1000");
        DECREASE_100 = register("surf.shop.item.sell-menu.decrease-100");
        DECREASE_10 = register("surf.shop.item.sell-menu.decrease-10");
        DECREASE_1 = register("surf.shop.item.sell-menu.decrease-1");
        INCREASE_1 = register("surf.shop.item.sell-menu.increase-1");
        INCREASE_10 = register("surf.shop.item.sell-menu.increase-10");
        INCREASE_100 = register("surf.shop.item.sell-menu.increase-100");
        INCREASE_1000 = register("surf.shop.item.sell-menu.increase-1000");
        SHOP_EDIT_SELL_PRICE = register("surf.shop.item.edit-menu.sell-price");
        SHOP_EDIT_BUY_PRICE = register("surf.shop.item.edit-menu.buy-price");
        SHOP_EDIT_AMOUNT = register("surf.shop.item.edit-menu.amount");
        SHOP_EDIT_MEMBERS = register("surf.shop.item.edit-menu.members");
        SHOP_EDIT_STORAGE = register("surf.shop.item.edit-menu.storage");
        SHOP_EDIT_DESCRIPTION = register("surf.shop.item.edit-menu.description");
        SHOP_EDIT_GLOBAL_LIMITS = register("surf.shop.item.edit-menu.global-limits");
        SHOP_EDIT_PLAYER_LIMITS = register("surf.shop.item.edit-menu.player-limits");
        SHOP_EDIT_BLOCK_PLAYER = register("surf.shop.item.edit-menu.block-player");
        SHOP_MENU_EDIT_MEMBERS_ADD = register("surf.shop.item.edit-menu.members.add");
        SHOP_MENU_EDIT_MEMBERS_REMOVE = register("surf.shop.item.edit-menu.members.remove");
        SHOP_MENU_EDIT_MEMBERS_LIST = register("surf.shop.item.edit-menu.members.list");
        SHOP_MENU_EDIT_STORAGE_ADD = register("surf.shop.item.edit-menu.storage.add");
        SHOP_MENU_EDIT_STORAGE_REMOVE = register("surf.shop.item.edit-menu.storage.remove");
        SHOP_MENU_EDIT_STORAGE_LIST = register("surf.shop.item.edit-menu.storage.list");
    }
}
