package dev.slne.shop.shop.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.shop.Shop;

public class ShopVisualizerTask extends BukkitRunnable {

    private Map<Shop, ShopVisualizer> visualizers;

    /**
     * A new {@link ShopVisualizerTask} instance
     */
    public ShopVisualizerTask() {
        this.visualizers = new HashMap<>();
    }

    @Override
    public void run() {
        cleanupVisualizers();

        List<Shop> shops = new ArrayList<>(BukkitApi.getInstance().getShopManager().getShops());

        for (Shop shop : shops) {
            if (!visualizers.containsKey(shop)) {
                addVisualizer(shop);
            }
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
        try {
            this.runTaskTimerAsynchronously(BukkitMain.getInstance(), 0, 1 * 20L);
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
    public Map<Shop, ShopVisualizer> getVisualizers() {
        return visualizers;
    }

    /**
     * Adds a visualizer
     *
     * @param shop the shop
     */
    private void addVisualizer(Shop shop) {
        visualizers.put(shop, new ShopVisualizer(shop));
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

}
