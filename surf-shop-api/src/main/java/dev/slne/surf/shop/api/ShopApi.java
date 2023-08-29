package dev.slne.surf.shop.api;

import dev.slne.surf.shop.api.instance.ShopInstance;
import dev.slne.surf.shop.api.shop.ShopManager;

import static com.google.common.base.Preconditions.*;

public final class ShopApi  {

    private static ShopInstance instance;

    public ShopApi(ShopInstance instance) {
        checkNotNull(instance, "instance cannot be null");
        checkState(ShopApi.instance == null, "Cannot create a new instance of the api");

        ShopApi.instance = instance;
    }

    public static ShopInstance getInstance() {
        return instance;
    }

    public static ShopManager getShopManager() {
        return instance.getShopManager();
    }
}
