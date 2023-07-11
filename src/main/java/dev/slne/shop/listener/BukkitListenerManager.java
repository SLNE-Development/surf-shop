package dev.slne.shop.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.listener.listeners.ShopBreakListener;
import dev.slne.shop.listener.listeners.ShopExplodeListener;
import dev.slne.shop.listener.listeners.ShopHopperListener;
import dev.slne.shop.listener.listeners.ShopPistonListener;
import dev.slne.shop.listener.listeners.ShopPlaceListener;
import dev.slne.shop.listener.listeners.ShopWaterlogListener;

public class BukkitListenerManager {

    /**
     * Registers all plugin {@link org.bukkit.event.Listener}s
     */
    public void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        JavaPlugin plugin = BukkitMain.getInstance();

        pluginManager.registerEvents(new ShopBreakListener(), plugin);
        pluginManager.registerEvents(new ShopPlaceListener(), plugin);
        pluginManager.registerEvents(new ShopExplodeListener(), plugin);
        pluginManager.registerEvents(new ShopPistonListener(), plugin);
        pluginManager.registerEvents(new ShopWaterlogListener(), plugin);
        pluginManager.registerEvents(new ShopHopperListener(), plugin);
    }

    /**
     * Unregisters all {@link org.bukkit.event.Listener}s
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(BukkitMain.getInstance());
    }

}
