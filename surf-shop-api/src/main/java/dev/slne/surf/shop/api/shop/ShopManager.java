package dev.slne.surf.shop.api.shop;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ShopManager {

    CompletableFuture<List<Shop>> fetchShops();
}
