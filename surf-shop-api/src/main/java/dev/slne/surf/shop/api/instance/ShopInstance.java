package dev.slne.surf.shop.api.instance;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.ShopManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface ShopInstance {
    void onLoad();

    void onEnable();

    void onDisable();

    ShopManager getShopManager();

    Shop createShop(UUID owner, ItemStack itemStack, Location location);

    ItemStack constructCreationItem();

    boolean isShop(Location location);

    boolean isShop(Block block);

    boolean isShop(BlockState blockState);

    boolean isShopItem(ItemStack itemStack);

    Shop getShop(Location location);

    Shop getShop(Block block);
}
