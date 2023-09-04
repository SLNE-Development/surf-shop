package dev.slne.surf.shop.api.instance;

import dev.slne.data.api.gson.GsonConverter;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.transaction.api.currency.Currency;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ShopInstance {

    /**
     * Called when the plugin is loaded
     */
    void onLoad();

    /**
     * Called when the plugin is enabled
     */
    void onEnable();

    /**
     * Called when the plugin is disabled
     */
    void onDisable();

    /**
     * Gets the shop manager
     *
     * @return the shop manager
     */
    ShopManager getShopManager();

    /**
     * Creates a shop with the specified owner, item stack, and location.
     * <p>
     * <b>IMPORTANT:</b> This method requires that the {@link BlockState}
     * at the specified location is a {@link org.bukkit.block.Chest}. If
     * it is not, this method will throw an {@link IllegalArgumentException}.
     *
     * @param owner     The owner of the shop.
     * @param itemStack The item stack of the shop.
     * @param location  The location of the shop.
     *
     * @return A {@link CompletableFuture} that completes with the created shop.
     */
    CompletableFuture<Shop> createShop(@NotNull Currency currency, @NotNull Player owner, ItemStack itemStack,
                                       Location location);

    /**
     * Creates a shop transaction
     *
     * @param shop   the shop
     * @param uuid   the uuid
     * @param amount the amount
     *
     * @return a {@link CompletableFuture} that completes with the created shop transaction
     */
    ShopTransaction createShopTransaction(Shop shop, UUID uuid, int amount);

    /**
     * Constructs the creation item
     *
     * @return the creation item
     */
    ItemStack constructCreationItem();

    /**
     * Gets the shop at the specified location.
     *
     * @param location the location
     *
     * @return the shop
     */
    boolean isShop(Location location);

    /**
     * Checks if the specified block is a shop
     *
     * @param block the block
     *
     * @return true if the block is a shop
     */
    boolean isShop(Block block);

    /**
     * Checks if the specified block state is a shop
     *
     * @param blockState the block state
     *
     * @return true if the block state is a shop
     */
    boolean isShop(BlockState blockState);

    /**
     * Checks if the specified item stack is a shop item
     *
     * @param itemStack the item stack
     *
     * @return true if the item stack is a shop item
     */
    boolean isShopItem(ItemStack itemStack);

    /**
     * Gets the shop at the specified location
     *
     * @param location the location
     *
     * @return the shop
     */
    Shop getShop(Location location);

    /**
     * Gets the shop at the specified block
     *
     * @param block the block
     *
     * @return the shop
     */
    Shop getShop(Block block);

    /**
     * Returns the default currency
     *
     * @return the default currency
     */
    Currency getDefaultCurrency();

    /**
     * Gets the other currencies
     *
     * @return the other currencies
     */
    List<Currency> getOtherCurrencies();

    /**
     * Gets the gson converter
     *
     * @return the gson converter
     */
    GsonConverter getGsonConverter();
}
