package dev.slne.surf.shop.server.instance;

import dev.slne.surf.shop.server.command.BukkitCommandManager;
import dev.slne.surf.shop.server.listener.BukkitListenerManager;
import dev.slne.surf.shop.server.shop.ShopManager;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private ShopManager shopManager;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        commandManager = new BukkitCommandManager();
        listenerManager = new BukkitListenerManager();

        shopManager = new ShopManager();
        shopManager.onLoad();
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        commandManager.registerCommands();
        listenerManager.registerListeners();

        shopManager.onEnable();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        listenerManager.unregisterListeners();

        shopManager.onDisable();
    }

    /**
     * Returns the {@link BukkitListenerManager}
     *
     * @return the {@link BukkitListenerManager}
     */
    public BukkitListenerManager getListenerManager() {
        return listenerManager;
    }

    /**
     * @return the shopManager
     */
    public ShopManager getShopManager() {
        return shopManager;
    }

}
