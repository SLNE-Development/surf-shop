package dev.slne.surf.shop.api.instance;

import dev.slne.surf.shop.api.shop.ShopManager;

public interface ShopInstance {
    void onLoad();

    void onEnable();

    void onDisable();

    ShopManager getShopManager();
}
