package dev.slne.surf.shop.server.listener;

import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.listener.listeners.ShopBreakListener;
import dev.slne.surf.shop.server.listener.listeners.ShopHopperListener;
import dev.slne.surf.shop.server.listener.listeners.ShopPlaceListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.slne.surf.shop.server.listener.listeners.ShopExplodeListener;
import dev.slne.surf.shop.server.listener.listeners.ShopInteractListener;
import dev.slne.surf.shop.server.listener.listeners.ShopPistonListener;
import dev.slne.surf.shop.server.listener.listeners.ShopWaterlogListener;

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
        pluginManager.registerEvents(new ShopInteractListener(), plugin);
    }

    /**
     * Unregisters all {@link org.bukkit.event.Listener}s
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(BukkitMain.getInstance());
    }

}
