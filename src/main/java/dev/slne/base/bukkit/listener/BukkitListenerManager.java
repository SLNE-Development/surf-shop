package dev.slne.base.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.slne.base.bukkit.BukkitMain;

public class BukkitListenerManager {

    /**
     * Registers all plugin {@link org.bukkit.event.Listener}s
     */
    @SuppressWarnings("unused")
    public void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        JavaPlugin plugin = BukkitMain.getInstance();
    }

    /**
     * Unregisters all {@link org.bukkit.event.Listener}s
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(BukkitMain.getInstance());
    }

}
