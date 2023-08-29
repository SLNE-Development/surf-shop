package dev.slne.surf.shop.server.shop.visualizer;

import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.server.shop.Shop;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ShopVisualizerTaskAsync {
    private ScheduledTask task = null;
    private final Map<Shop, ShopVisualizer> visualizers;

    public ShopVisualizerTaskAsync() {
        this.visualizers = new ConcurrentHashMap<>();
    }

    /**
     * Runs the task
     */
    private void run() {
        cleanupVisualizers();

        List<Shop> shops = new ArrayList<>(BukkitApi.getInstance().getShopManager().getShops());

        for (Shop shop : shops) {
            visualizers.computeIfAbsent(shop, key -> new ShopVisualizer(shop));
        }

        for (ShopVisualizer visualizer : new ArrayList<>(visualizers.values())) {
            visualizer.update();
        }
    }

    /**
     * Cleanup the visualizers
     */
    private void cleanupVisualizers() {
        List<Shop> shops = new ArrayList<>(BukkitApi.getInstance().getShopManager().getShops());

        for (Shop shop : new ArrayList<>(visualizers.keySet())) {
            if (!shops.contains(shop)) {
                removeVisualizer(shop);
            }
        }
    }

    /**
     * Starts the task
     */
    public void start() {
        Bukkit.getAsyncScheduler().runAtFixedRate(BukkitMain.getInstance(), scheduledTask -> {
            task = scheduledTask;
            run();
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops the task
     */
    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }


    /**
     * Removes a visualizer
     *
     * @param shop the shop
     */
    private void removeVisualizer(Shop shop) {
        if (visualizers.containsKey(shop)) {
            visualizers.get(shop).despawnAll();
        }

        visualizers.remove(shop);
    }

    /**
     * Adds a player to the visualizers now
     *
     * @param player the player
     * @param shop   the shop
     */
    public void addPlayer(Player player, Shop shop) {
        if (visualizers.containsKey(shop)) {
            visualizers.get(shop).spawn(player);
        }
    }

    /**
     * @return the visualizers
     */
    public Map<Shop, ShopVisualizer> getVisualizers() {
        return visualizers;
    }
}
