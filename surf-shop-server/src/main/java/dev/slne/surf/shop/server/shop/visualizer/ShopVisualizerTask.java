package dev.slne.surf.shop.server.shop.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.slne.surf.shop.server.instance.BukkitApi;

public class ShopVisualizerTask extends BukkitRunnable {

    private Map<ServerShop, ShopVisualizer> visualizers;

    /**
     * A new {@link ShopVisualizerTask} instance
     */
    public ShopVisualizerTask() {
        this.visualizers = new HashMap<>();
    }

    @Override
    public void run() {
        cleanupVisualizers();

        List<ServerShop> shops = new ArrayList<>(BukkitApi.getInstance().getShopManager().getShops());

        for (ServerShop shop : shops) {
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
        List<ServerShop> shops = new ArrayList<>(BukkitApi.getInstance().getShopManager().getShops());

        for (ServerShop shop : new ArrayList<>(visualizers.keySet())) {
            if (!shops.contains(shop)) {
                removeVisualizer(shop);
            }
        }
    }

    /**
     * Starts the task
     */
    public void start() {
        try {
            this.runTaskTimer(BukkitMain.getInstance(), 0, 1 * 20L);
        } catch (Exception exception) {
            // IGNORE
        }
    }

    /**
     * Stops the task
     */
    public void stop() {
        try {
            if (!this.isCancelled()) {
                this.cancel();
            }
        } catch (Exception exception) {
            // IGNORE
        }
    }

    /**
     * @return the visualizers
     */
    public Map<ServerShop, ShopVisualizer> getVisualizers() {
        return visualizers;
    }

    /**
     * Removes a visualizer
     *
     * @param shop the shop
     */
    private void removeVisualizer(ServerShop shop) {
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
    public void addPlayer(Player player, ServerShop shop) {
        if (visualizers.containsKey(shop)) {
            visualizers.get(shop).spawn(player);
        }
    }
}
