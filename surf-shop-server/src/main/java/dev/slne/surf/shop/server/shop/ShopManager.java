package dev.slne.surf.shop.server.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.server.shop.visualizer.ShopVisualizerTask;
import org.bukkit.entity.Player;

public class ShopManager {

    private List<ServerShop> shops;
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
     * Fetches all {@link ServerShop} instances from the database
     *
     * @return the {@link CompletableFuture} instance
     */
    public CompletableFuture<List<ServerShop>> fetchShops() {
        this.fetched = false;

        return ServerShop.shops().thenApplyAsync(fetchedShops -> {
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
    public List<ServerShop> getShops() {
        return shops;
    }

    /**
     * Returns a list of {@link ServerShop} instances by the given {@link Player}
     *
     * @param player the player
     * @return the list of {@link ServerShop} instances
     */
    public List<ServerShop> getShops(Player player) {
        return shops.stream().filter(shop -> shop.getOwner() != null && shop.getOwner().equals(player))
                .toList();
    }

    /**
     * Returns a {@link ServerShop} instance by the given {@link UUID}
     *
     * @param uuid the uuid
     * @return the {@link ServerShop} instance
     */
    public ServerShop getShop(UUID uuid) {
        return shops.stream().filter(shop -> shop.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Returns a {@link ServerShop} instance by the given id
     *
     * @param id the id
     * @return the {@link ServerShop} instance
     */
    public ServerShop getShop(long id) {
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
     * Adds a {@link ServerShop} instance to the list
     *
     * @param shop the shop
     */
    public void addShop(ServerShop shop) {
        shops.add(shop);
    }

    /**
     * Removes a {@link ServerShop} instance from the list
     *
     * @param shop the shop
     */
    public void removeShop(ServerShop shop) {
        shops.remove(shop);
    }

    /**
     * @return the visualizerTask
     */
    public ShopVisualizerTask getVisualizerTask() {
        return visualizerTask;
    }

}
