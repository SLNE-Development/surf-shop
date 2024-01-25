package dev.slne.surf.shop.api.shop;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ShopManager {

    CompletableFuture<List<Shop>> fetchShops();

    List<Shop> getShops();

    List<Shop> getShopsByOwner(UUID owner);

    List<Shop> getShopsByOwner(OfflinePlayer owner);

    Shop getShop(UUID uuid);

    Shop getShop(long id);

    boolean isFetched();

    void addShop(Shop shop);

    void removeShop(Shop shop);

    void makeShop(Chest chest, Shop shop);

    CompletableFuture<Shop> createShop(Shop shop);
}
