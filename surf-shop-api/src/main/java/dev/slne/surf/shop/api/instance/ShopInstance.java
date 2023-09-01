package dev.slne.surf.shop.api.instance;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import dev.slne.transaction.api.currency.Currency;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ShopInstance {

    void onLoad();

    void onEnable();

    void onDisable();

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
     * @return A {@link CompletableFuture} that completes with the created shop.
     */
    CompletableFuture<Shop> createShop(@NotNull Currency currency, @NotNull Player owner, ItemStack itemStack, Location location);

    ItemStack constructCreationItem();

    boolean isShop(Location location);

    boolean isShop(Block block);

    boolean isShop(BlockState blockState);

    boolean isShopItem(ItemStack itemStack);

    Shop getShop(Location location);

    Shop getShop(Block block);

    Currency getDefaultCurrency();

    List<Currency> getOtherCurrencies();
}
