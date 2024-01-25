package dev.slne.surf.shop.server.shop;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.shop.visualizer.ShopVisualizerTask;
import dev.slne.surf.shop.server.spring.repository.jpa.ShopRepository;
import dev.slne.surf.shop.server.util.UuidDataType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;

public class ServerShopManager implements ShopManager {

    private Cache<UUID, Shop> shopCache = Caffeine.newBuilder()
            .build();
    /**
     * -- GETTER --
     *
     * @return the fetched
     */
    @Getter
    private boolean fetched;
    /**
     * -- GETTER --
     *
     * @return the visualizerTask
     */
    @Getter
    private final ShopVisualizerTask visualizerTask;

    /**
     * A new {@link ServerShopManager} instance
     */
    public ServerShopManager() {
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

            shopCache.putAll(fetchedShops.stream()
                    .collect(Collectors.toMap(Shop::getUuid, shop -> shop)));
            this.fetched = true;

            DataApi.getDataInstance().logInfo(getClass(), "Fetched " + fetchedShops.size() + " shops");

            return fetchedShops;
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to fetch shops", throwable);
            return null;
        });
    }

    public CompletableFuture<Void> saveShops() {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        for (Shop shop : shops) {
            future.thenComposeAsync(__ -> shop.save());
        }

        return future;
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
        ShopApi.getContext().getBean(ShopRepository.class)


        CacheManager cacheManager = ShopApi.getContext().getBean(CacheManager.class);
        Cache<Object, Object> nativeCache = cacheManager.getCache().getNativeCache();
        Map<Object, Object> all = nativeCache.getAll();

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
                .filter(shop -> Objects.equals(shop.getUuid(), uuid))
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
            chest.getPersistentDataContainer().set(Shop.CREATED_SHOP_KEY, UuidDataType.UUID, shop.getUuid());
            chest.update();
        });
    }

    @Override
    public CompletableFuture<Shop> createShop(Shop shop) {
        checkNotNull(shop, "Shop cannot be null");
        final Shop cachedShop = shopCache.getIfPresent(shop.getUuid());

        if (cachedShop != null) {
            return CompletableFuture.completedFuture(cachedShop);
        }

        return shop.save().thenApplyAsync(updatedShop -> {
            shopCache.put(updatedShop.getUuid(), updatedShop);
            return updatedShop;
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop", throwable);
            return null;
        });
    }
}
