package dev.slne.shop.bukkit.instance;

import dev.slne.shop.bukkit.command.BukkitCommandManager;
import dev.slne.shop.bukkit.listener.BukkitListenerManager;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        commandManager = new BukkitCommandManager();
        listenerManager = new BukkitListenerManager();
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        commandManager.registerCommands();
        listenerManager.registerListeners();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        listenerManager.unregisterListeners();
    }

    /**
     * Returns the {@link BukkitListenerManager}
     *
     * @return the {@link BukkitListenerManager}
     */
    public BukkitListenerManager getListenerManager() {
        return listenerManager;
    }

}
