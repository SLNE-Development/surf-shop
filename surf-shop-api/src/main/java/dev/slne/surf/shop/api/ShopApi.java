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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class ShopApi {

    private static ShopInstance instance;

    /**
     * Creates a new instance of the api
     *
     * @param instance the instance
     */
    public ShopApi(ShopInstance instance) {
        checkNotNull(instance, "instance cannot be null");
        checkState(ShopApi.instance == null, "Cannot create a new instance of the api");

        ShopApi.instance = instance;
    }

    /**
     * Returns the instance of the api
     *
     * @return the instance
     */
    public static ShopInstance getInstance() {
        return instance;
    }

    /**
     * Returns the shop manager
     *
     * @return the shop manager
     */
    public static ShopManager getShopManager() {
        return instance.getShopManager();
    }

    /**
     * Returns the shop with the specified uuid
     *
     * @param uuid the uuid
     *
     * @return the shop
     */
    public static Shop getShop(UUID uuid) {
        return instance.getShopManager().getShop(uuid);
    }

    /**
     * Returns the shop with the specified id
     *
     * @param id the id
     *
     * @return the shop
     */
    public static Shop getShop(long id) {
        return instance.getShopManager().getShop(id);
    }

    /**
     * Returns all the shops
     *
     * @return all the shops
     */
    public static List<Shop> getShops() {
        return instance.getShopManager().getShops();
    }

    /**
     * Returns all the shops with the specified owner
     *
     * @param owner the owner
     *
     * @return all the shops with the specified owner
     */
    public static List<Shop> getShopsByOwner(UUID owner) {
        return instance.getShopManager().getShopsByOwner(owner);
    }

    /**
     * Returns all the shops with the specified owner
     *
     * @param owner the owner
     *
     * @return all the shops with the specified owner
     */
    public static List<Shop> getShopsByOwner(OfflinePlayer owner) {
        return instance.getShopManager().getShopsByOwner(owner);
    }

    /**
     * Returns the constructed creation item
     *
     * @return the constructed creation item
     */
    public static ItemStack constructCreationItem() {
        return instance.constructCreationItem();
    }

    /**
     * Returns the shop at the given block
     *
     * @param block the block
     *
     * @return the shop
     */
    public static Shop getShop(Block block) {
        return instance.getShop(block);
    }

    /**
     * Returns the shop at the given location
     *
     * @param location the location
     *
     * @return the shop
     */
    public static Shop getShop(Location location) {
        return instance.getShop(location);
    }

    /**
     * Returns if the given block is a shop
     *
     * @param block the block
     *
     * @return if the given block is a shop
     */
    public static boolean isShop(Block block) {
        return instance.isShop(block);
    }

    /**
     * Returns if the given location is a shop
     *
     * @param location the location
     *
     * @return if the given location is a shop
     */
    public static boolean isShop(Location location) {
        return instance.isShop(location);
    }

    /**
     * Returns if the given block state is a shop
     *
     * @param blockState the block state
     *
     * @return if the given block state is a shop
     */
    public static boolean isShop(BlockState blockState) {
        return instance.isShop(blockState);
    }

    /**
     * Returns if the given item stack is a shop item
     *
     * @param itemStack the item stack
     *
     * @return if the given item stack is a shop item
     */
    public static boolean isShopItem(ItemStack itemStack) {
        return instance.isShopItem(itemStack);
    }
}
