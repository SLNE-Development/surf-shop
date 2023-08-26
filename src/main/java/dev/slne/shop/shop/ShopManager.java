package dev.slne.shop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dev.slne.data.api.DataApi;
import dev.slne.shop.shop.visualizer.ShopVisualizer;
import dev.slne.shop.shop.visualizer.ShopVisualizerTask;
import dev.slne.shop.shop.visualizer.ShopVisualizerTaskAsync;
import org.bukkit.entity.Player;

public class ShopManager {

    private List<Shop> shops;
    private boolean fetched;
    private final ShopVisualizerTask visualizerTask;

    /**
     * A new {@link ShopManager} instance
     */
    public ShopManager() {
        this.shops = new ArrayList<>();
        this.fetched = false;
        this.visualizerTask = new ShopVisualizerTask();
    }

    /**
     * Fetches all {@link Shop} instances from the database
     *
     * @return the {@link CompletableFuture} instance
     */
    public CompletableFuture<List<Shop>> fetchShops() {
        this.fetched = false;

        return Shop.shops().thenApplyAsync(fetchedShops -> {
            if (fetchedShops == null) {
                throw new NullPointerException("Fetch shops returned null");
            }

            this.shops = fetchedShops;
            this.fetched = true;

            DataApi.getDataInstance().logInfo(getClass(), "Fetched " + fetchedShops.size() + " shops");

            return fetchedShops;
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to fetch shops", throwable);
            return null;
        });
    }

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        fetchShops();
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        visualizerTask.start();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        visualizerTask.stop();
    }

    /**
     * @return the shops
     */
    public List<Shop> getShops() {
        return shops;
    }

    /**
     * Returns a list of {@link Shop} instances by the given {@link Player}
     *
     * @param player the player
     * @return the list of {@link Shop} instances
     */
    public List<Shop> getShops(Player player) {
        return shops.stream().filter(shop -> shop.getOwner() != null && shop.getOwner().equals(player))
                .toList();
    }

    /**
     * Returns a {@link Shop} instance by the given {@link UUID}
     *
     * @param uuid the uuid
     * @return the {@link Shop} instance
     */
    public Shop getShop(UUID uuid) {
        return shops.stream().filter(shop -> shop.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Returns a {@link Shop} instance by the given id
     *
     * @param id the id
     * @return the {@link Shop} instance
     */
    public Shop getShop(long id) {
        return shops.stream().filter(shop -> shop.getId() == id && id != 0 && shop.getId() != 0).findFirst()
                .orElse(null);
    }

    /**
     * @return the fetched
     */
    public boolean isFetched() {
        return fetched;
    }

    /**
     * Adds a {@link Shop} instance to the list
     *
     * @param shop the shop
     */
    public void addShop(Shop shop) {
        shops.add(shop);
    }

    /**
     * Removes a {@link Shop} instance from the list
     *
     * @param shop the shop
     */
    public void removeShop(Shop shop) {
        shops.remove(shop);
    }

    /**
     * @return the visualizerTask
     */
    public ShopVisualizerTask getVisualizerTask() {
        return visualizerTask;
    }

}
