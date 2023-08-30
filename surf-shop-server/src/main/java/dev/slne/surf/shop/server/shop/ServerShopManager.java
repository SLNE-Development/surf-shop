package dev.slne.surf.shop.server.shop;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.shop.visualizer.ShopVisualizerTask;
import dev.slne.surf.shop.server.util.UUIDDataType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerShopManager implements ShopManager {

    private List<Shop> shops;
    private boolean fetched;
    private final ShopVisualizerTask visualizerTask;

    /**
     * A new {@link ServerShopManager} instance
     */
    public ServerShopManager() {
        this.shops = new ArrayList<>();
        this.fetched = false;
        this.visualizerTask = new ShopVisualizerTask();
    }

    /**
     * Fetches all {@link ServerShop} instances from the database
     *
     * @return the {@link CompletableFuture} instance
     */
    public CompletableFuture<List<Shop>> fetchShops() {
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
    public List<Shop> getShops() {
        return shops;
    }

    @Override
    public List<Shop> getShopsByOwner(UUID owner) {
        return Stream.of(shops)
                .flatMap(List::stream)
                .filter(shop -> shop.getOwner().getUniqueId().equals(owner))
                .collect(Collectors.toList());
    }

    @Override
    public List<Shop> getShopsByOwner(@NotNull OfflinePlayer owner) {
        return getShopsByOwner(owner.getUniqueId());
    }

    /**
     * Returns a {@link ServerShop} instance by the given {@link UUID}
     *
     * @param uuid the uuid
     * @return the {@link ServerShop} instance
     */
    @Override
    public Shop getShop(UUID uuid) {
        return shops.stream()
                .filter(shop -> Objects.equals(shop.getUUID(), uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a {@link ServerShop} instance by the given id
     *
     * @param id the id
     * @return the {@link ServerShop} instance
     */
    public Shop getShop(long id) {
        return shops.stream()
                .filter(shop -> shop.getId() == id && id != 0 && shop.getId() != 0).findFirst()
                .orElse(null);
    }

    /**
     * @return the fetched
     */
    public boolean isFetched() {
        return fetched;
    }

    @Override
    public void addShop(Shop shop) {
        shops.add(shop);
    }

    @Override
    public void removeShop(Shop shop) {
        shops.remove(shop);
    }

    @Override
    public void makeShop(Chest chest, Shop shop) {
        Bukkit.getScheduler().runTask(BukkitMain.getInstance(), () -> {
            chest.getPersistentDataContainer().set(Shop.CREATED_SHOP_KEY, UUIDDataType.UUID, shop.getUUID());
            chest.update();
        });
    }

    /**
     * @return the visualizerTask
     */
    public ShopVisualizerTask getVisualizerTask() {
        return visualizerTask;
    }
}
