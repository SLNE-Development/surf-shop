package dev.slne.surf.shop.api;

import dev.slne.surf.shop.api.instance.ShopInstance;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;

public final class ShopApi {

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

    public static Shop getShop(UUID uuid) {
        return instance.getShopManager().getShop(uuid);
    }

    public static Shop getShop(long id) {
        return instance.getShopManager().getShop(id);
    }

    public static List<Shop> getShops() {
        return instance.getShopManager().getShops();
    }

    public static List<Shop> getShopsByOwner(UUID owner) {
        return instance.getShopManager().getShopsByOwner(owner);
    }

    public static List<Shop> getShopsByOwner(OfflinePlayer owner) {
        return instance.getShopManager().getShopsByOwner(owner);
    }

    public static ItemStack constructCreationItem() {
        return instance.constructCreationItem();
    }

    public static Shop getShop(Block block) {
        return instance.getShop(block);
    }

    public static Shop getShop(Location location) {
        return instance.getShop(location);
    }

    public static boolean isShop(Block block) {
        return instance.isShop(block);
    }

    public static boolean isShop(Location location) {
        return instance.isShop(location);
    }

    public static boolean isShop(BlockState blockState) {
        return instance.isShop(blockState);
    }

    public static boolean isShopItem(ItemStack itemStack) {
        return instance.isShopItem(itemStack);
    }
}
