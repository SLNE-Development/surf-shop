package dev.slne.surf.shop.server;

import java.util.Random;

import dev.slne.surf.shop.server.instance.BukkitInstance;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;

import dev.slne.surf.shop.server.instance.BukkitApi;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public class BukkitMain extends JavaPlugin {

    private static BukkitMain instance;
    private static BukkitInstance bukkitInstance;

    private Random random;

    @Override
    @SuppressWarnings({ "java:S3252", "java:S2696" })
    public void onLoad() {
        instance = this;
        bukkitInstance = new BukkitInstance();
        BukkitApi.setInstance(bukkitInstance);

        random = new Random();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(true);
        PacketEvents.getAPI().load();

        bukkitInstance.onLoad();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        bukkitInstance.onEnable();
    }

    @Override
    public void onDisable() {
        bukkitInstance.onDisable();

        PacketEvents.getAPI().terminate();
    }

    /**
     * Returns the instance of the plugin
     *
     * @return The instance of the plugin
     */
    public static BukkitMain getInstance() {
        return instance;
    }

    /**
     * Returns the core instance of the plugin
     *
     * @return The core instance of the plugin
     */
    public static BukkitInstance getBukkitInstance() {
        return bukkitInstance;
    }

    /**
     * @return the random
     */
    public Random getRandom() {
        return random;
    }

}
