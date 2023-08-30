package dev.slne.surf.shop.server.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import dev.slne.surf.shop.server.shop.visualizer.ShopVisualizerTask;
import dev.slne.surf.shop.server.util.UUIDDataType;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ServerShopManager implements ShopManager {

    private List<ServerShop> shops;
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

            return fetchedShops.stream().map(ServerShop::inter).collect(Collectors.toList());
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
        return shops.stream().map(ServerShop::inter).toList();
    }

    @Override
    public List<Shop> getShopsByOwner(UUID owner) {
        return Stream.of(shops)
                .flatMap(List::stream)
                .filter(shop -> shop.getOwner().getUniqueId().equals(owner))
                .map(ServerShop::inter)
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

    @Override
    public void addShop(Shop shop) {
        if (!(shop instanceof ServerShop serverShop)) {
            throw new IllegalArgumentException("Shop is not a ServerShop");
        }

        shops.add(serverShop);
    }

    @Override
    public void removeShop(Shop shop) {
        if (!(shop instanceof ServerShop serverShop)) {
            throw new IllegalArgumentException("Shop is not a ServerShop");
        }

        shops.remove(serverShop);
    }

    @Override
    public void makeShop(Chest chest, Shop shop) {
        chest.getPersistentDataContainer().set(Shop.CREATED_SHOP_KEY, UUIDDataType.UUID, shop.getUUID());

        chest.update();
    }

    /**
     * @return the visualizerTask
     */
    public ShopVisualizerTask getVisualizerTask() {
        return visualizerTask;
    }
}
